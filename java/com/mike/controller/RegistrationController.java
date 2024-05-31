package com.mike.controller;

import com.mike.domain.Role;
import com.mike.domain.User;
import com.mike.service.ControllerUtils;
import com.mike.service.MailSenderService;
import com.mike.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
public class RegistrationController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MailSenderService mailSenderService;

    @Value("${actual.year}")
    private int actualYear;

    @ModelAttribute("valid")
    public boolean isValid(Principal principal) {
        if (!userService.isAuthenticates(principal)) {
            User currentUser = userService.loadUserByUsername(principal.getName());
            return !currentUser.getRoles().contains(Role.USER);
        }
        return false;
    }

    @ModelAttribute("admin")
    public boolean isAdmin(Principal principal) {
        if (!userService.isAuthenticates(principal)) {
            User currentUser = userService.loadUserByUsername(principal.getName());
            return currentUser.getRoles().contains(Role.ADMIN);
        }
        return false;
    }

    @ModelAttribute("authentic")
    public boolean authentic(Principal principal) {
        return userService.isAuthenticates(principal);
    }

    @ModelAttribute("roles")
    public List<Role> roleList() {
        return Arrays.asList(Role.values());
    }

    @GetMapping(path = "/registration")
    public String registration(HttpSession httpSession, @ModelAttribute("user") User user) {
        return "registration";
    }

    @PostMapping(path = "/registration")
    public String addUser(@RequestParam("confirmation") String confirmation,
                          @ModelAttribute("user") @Valid User user,
                          BindingResult result,
                          Model model,
                          RedirectAttributes redirectAttributes) {
        boolean isConfirmEmpty = StringUtils.isEmpty(confirmation);
        if (result.hasErrors() || isConfirmEmpty) {
            if (isConfirmEmpty) {
                model.addAttribute("confirmation", "Поле не может быть пустым");
            }
            if (!passwordEncoder.matches(confirmation, user.getPassword())) {
                model.addAttribute("mess", "Пароли не совпадают");
            }
            Map<String, String> errors = ControllerUtils.getErrors(result);
            System.out.println(errors);
            model.mergeAttributes(errors);
            model.addAttribute("message", "Ошибка регистрации");
            return "registration";
        }
        if (!userService.addUser(user) || !passwordEncoder.matches(confirmation, user.getPassword())) {
            if (!passwordEncoder.matches(confirmation, user.getPassword())) {
                model.addAttribute("mess", "Пароли не совпадают");
            }
            Map<String, String> errors = ControllerUtils.getErrors(result);
            System.out.println(errors);
            model.mergeAttributes(errors);
            model.addAttribute("message", "Ошибка регистрации");
            if (!userService.addUser(user)) {
                model.addAttribute("message", "Данный пользователь существует!");
            }
            return "registration";
        }

        Pattern blockNumber = Pattern.compile("\\d");
        Pattern blockSymbol = Pattern.compile("\\w");
        Pattern blockCapital = Pattern.compile("[A-ZА-Я]{2}");
        Matcher matcherNumber = blockNumber.matcher(user.getLastName());
        Matcher matcherSymbol = blockSymbol.matcher(user.getLastName());
        Matcher matcherCapital = blockCapital.matcher(user.getLastName());
        if (matcherNumber.find() || matcherSymbol.find() || matcherCapital.find()) {
            redirectAttributes.addFlashAttribute("message", "Пользователь заблокирован! " +
                    "Введены недопустимые данные! Для уточнения обратитесь к секретарю конференции.");
            return "redirect:/registration";
        }

        redirectAttributes.addFlashAttribute("messageOk", "Пользователь зарегистрирован. " +
                "Для активации профиля перейдите по ссылке на вашей почте!");
        return "redirect:/registration";
    }

    @GetMapping("/activate/{code}")
    public String activate(@ModelAttribute("user") User user,
                           @PathVariable String code,
                           Model model) {
        boolean isActivated = userService.activateUser(code);
        if (isActivated) {
            model.addAttribute("messageOk", "Пользователь успешно активирован");
        } else {
            model.addAttribute("message", "Код активации не найден!");
        }
        return "login";
    }

    @GetMapping(path = "/passwordReset")
    public String passwordReset() {
        return "passwordReset";
    }

    @PostMapping(path = "/passwordReset")
    public String passwordChangeMessage(@RequestParam("email") String email,
                                        Model model) {
        User user = userService.loadUserByUsername(email);
        if (user == null) {
            model.addAttribute("message", "Данный пользователь не зарегистрирован");
        } else {
            if (!user.isAccountNonLocked()) {
                model.addAttribute("message", "Данный пользователь заблокирован. Обратитесь к секретарю конференции");
                return "passwordReset";
            }
            mailSenderService.passwordChangeMessage(user);
            model.addAttribute("messageOk", "Для изменения пароля перейдите по ссылке на вашей почте");
        }
        return "passwordReset";
    }

    @GetMapping(path = "/passwordChange/{code}")
    public String passwordReset(@PathVariable String code,
                                @ModelAttribute("user") User user,
                                Model model) {
        model.addAttribute("code", code);
        return "passwordChange";
    }

    @PostMapping(path = "/passwordChange/*")
    public String passwordReset(@RequestParam("confirmation") String confirmation,
                                @RequestParam("password") String password,
                                @RequestParam("code") String code,
                                @ModelAttribute("user") @Valid User user,
                                BindingResult result,
                                Model model) {
        boolean isPasswordEmpty = StringUtils.isEmpty(password);
        boolean isConfirmEmpty = StringUtils.isEmpty(confirmation);

        if (isPasswordEmpty || isConfirmEmpty) {
            if (isConfirmEmpty) {
                model.addAttribute("confirmation", "Поле не может быть пустым");
            }
            if (password.length() < 6) {
                model.addAttribute("mess", "Пароль не менее 6 знаков");
            }
            if (isPasswordEmpty) {
                model.addAttribute("password", "Поле не может быть пустым");
            }
            model.addAttribute("code", code);
            return "passwordChange";
        }
        if (!password.equals(confirmation)) {
            model.addAttribute("mess", "Пароли не совпадают");
            model.addAttribute("code", code);
            return "passwordChange";
        }
        Map<String, String> errors = ControllerUtils.getErrors(result);
        System.out.println(errors);
        model.mergeAttributes(errors);
        if (userService.loadUserByActivationCode(code) != null) {
            userService.passwordChange(password, code);
            model.addAttribute("messageOk", "Новый пароль успешно сохранен");
            return "login";
        }
        model.addAttribute("message", "Пользователь не найден");
        return "passwordChange";
    }

    @PostMapping(path = "/setActualYear")
    public String setActualYear(Principal principal) {
        User currentUser = userService.loadUserByUsername(principal.getName());
        currentUser.setActualYear(actualYear);
        return "redirect:/personal";
    }
}
