package com.mike.controller;

import com.mike.domain.Affiliation;
import com.mike.domain.PresentationType;
import com.mike.domain.Publication;
import com.mike.domain.User;
import com.mike.repos.UserRepo;
import com.mike.service.ControllerUtils;
import com.mike.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

@Controller
@RequestMapping("/En")
public class EnglishController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepo userRepo;

    @Value("${static}")
    private String staticPath;


    @GetMapping(path = "/")
    public String mainEn() {
        return "En/mainPageEn";
    }

    @GetMapping(path = "/publications/announcementEn")
    public ResponseEntity<Resource> downloadAnnouncementEn(String param) throws IOException {
        File file = new File(staticPath + "/Second_Announcement_2023_rus.pdf");
        HttpHeaders headers = new HttpHeaders();
        InputStreamResource resource = new InputStreamResource(new FileInputStream(file));

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(file.length())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    @GetMapping(path = "/registrationEn")
    public String getRegistrationEn(@ModelAttribute("affiliation") Affiliation affiliation,
                                    @ModelAttribute("publication") Publication publication,
                                    @ModelAttribute("user") User user,
                                    Model model) {
        Set<PresentationType> types = EnumSet.allOf(PresentationType.class);
        model.addAttribute("types", types);
        String section = "";
        model.addAttribute("section", section);
        return "En/registrationEn";
    }

    @PostMapping(path = "/registrationEn")
    public String registrationEn(@ModelAttribute("affiliation") @Valid Affiliation affiliation,
                                 BindingResult affilResult,
                                 @ModelAttribute("publication") @Valid Publication publication,
                                 BindingResult publResult,
                                 @ModelAttribute("user") @Valid User user,
                                 BindingResult userResult,
                                 @RequestParam("section") String section,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {

        if (affilResult.hasErrors() || publResult.hasErrors() || userResult.hasErrors()) {
            Map<String, String> affilErrors = ControllerUtils.getErrors(affilResult);
            Map<String, String> publErrors = ControllerUtils.getErrors(publResult);
            Map<String, String> userErrors = ControllerUtils.getErrors(userResult);
            model.mergeAttributes(affilErrors);
            model.mergeAttributes(publErrors);
            model.mergeAttributes(userErrors);
            System.out.println(affilErrors);
            System.out.println(publErrors);
            System.out.println(userErrors);
            model.addAttribute("message", "Registration error");
            return "En/registrationEn";
        }
        User userFromDb = userRepo.findByEmail(user.getEmail());

        if (userFromDb != null) {
            model.addAttribute("message", "This user exists");
            return "En/registrationEn";
        }

        if (!userService.addUserEn(user, affiliation, publication, section)) {
            model.addAttribute("message", "Section not selected");
            return "En/registrationEn";
        }
        redirectAttributes.addFlashAttribute("messageOk", "Registration completed successfully");
        return "redirect:/En/registrationEn";
    }

    @GetMapping(path = "/publicationsEn")
    public String publicationsEn() {
        return "En/publicationsEn";
    }

    @GetMapping(path = "/venueEn")
    public String venueEn() {
        return "En/venueEn";
    }

//    @GetMapping(path = "/feeEn")
//    public String feeEn() {
//        return "venueEn";
//    }

    @GetMapping(path = "/contactsEn")
    public String contactsEn() {
        return "En/contactsEn";
    }

}
