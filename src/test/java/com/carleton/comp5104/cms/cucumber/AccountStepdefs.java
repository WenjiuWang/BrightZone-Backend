package com.carleton.comp5104.cms.cucumber;

import com.carleton.comp5104.cms.controller.account.AccountController;
import com.carleton.comp5104.cms.entity.Account;
import com.carleton.comp5104.cms.enums.AccountStatus;
import com.carleton.comp5104.cms.repository.AccountRepository;
import com.carleton.comp5104.cms.service.AccountService;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@SpringBootTest
public class AccountStepdefs {

    private int userId = 0;
    private String password = "";
    private Account account = new Account();
    private Map<String, Object> resultMap = new HashMap<>();

    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AccountController accountController;

    private MockHttpServletRequest mockHttpServletRequest;


    @Given("User input userId {int} and password {string} in the login form")
    public void user_input_user_id_and_password_in_the_login_form(Integer userId, String password) {
        this.userId = userId;
        this.password = password;
    }

    @Given("User is not a member in the university")
    public void user_is_not_a_member_in_the_university() {
        Assert.assertFalse(accountRepository.existsById(userId));
    }

    @Given("UserId {int} is not authorized yet")
    public void user_id_is_not_authorized_yet(Integer userId) {
        Optional<Account> optionalAccount = accountRepository.findById(userId);
        Assert.assertTrue(optionalAccount.isPresent());
        this.account = optionalAccount.get();
        Assert.assertEquals(AccountStatus.unauthorized, this.account.getAccountStatus());
    }

    @Given("Email {string} is already authorized")
    public void email_is_already_authorized(String email) {
        Optional<Account> optionalAccount = accountRepository.findByEmail(email);
        Assert.assertTrue(optionalAccount.isPresent());
        this.account = optionalAccount.get();
        Assert.assertNotEquals(AccountStatus.unauthorized, this.account.getAccountStatus());
    }

    @Given("Password {string} is not correct")
    public void password_is_not_correct(String password) {
        Assert.assertNotEquals(this.account.getPassword(), password);
    }

    @Given("Password {string} is correct")
    public void password_is_correct(String password) {
        Assert.assertEquals(this.account.getPassword(), password);
    }


    @When("User hit login button")
    public void user_hit_login_button() {
        this.resultMap = accountService.login(Integer.toString(userId), password);
        boolean success = (boolean) this.resultMap.get("success");
        if (success) {
            this.account = (Account) this.resultMap.get("account");
        }
        System.out.println("result map: " + this.resultMap);
    }

    @Then("Login fail")
    public void login_fail() {
        Assert.assertFalse((boolean) this.resultMap.get("success"));
    }

    @Then("Login success")
    public void login_success() {
        Assert.assertTrue((boolean) this.resultMap.get("success"));
    }

    @Given("User hit logout button")
    public void user_hit_logout_button() {
        HttpSession session = mockHttpServletRequest.getSession(false);
        if (session != null) {
            System.out.println(session.getAttribute("userId"));
        }
        this.resultMap = accountController.logout(mockHttpServletRequest);
        System.out.println(this.resultMap);
    }

    @When("User did not login to Course Management System before")
    public void user_did_not_login_to_course_management_system_before() {
        mockHttpServletRequest = new MockHttpServletRequest();
    }

    @Then("Logout fail")
    public void logout_fail() {
        Assert.assertFalse((Boolean) this.resultMap.get("success"));
    }

    @Given("User login to Course Management System before")
    public void user_login_to_course_management_system_before() {
        mockHttpServletRequest = new MockHttpServletRequest();
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", "1000000");
        mockHttpServletRequest.setSession(session);
    }

    @Then("Logout success")
    public void logout_success() {
        Assert.assertTrue((Boolean) this.resultMap.get("success"));
    }

}
