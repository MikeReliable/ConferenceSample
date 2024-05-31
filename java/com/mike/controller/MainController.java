package com.mike.controller;

import com.mike.domain.PresentationType;
import com.mike.domain.Publication;
import com.mike.domain.Role;
import com.mike.domain.User;
import com.mike.service.PublicationService;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@Controller
public class MainController {

    @Value("${static}")
    private String staticPath;

    @Autowired
    private UserService userService;

    @Autowired
    private PublicationService publicationService;

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

    @GetMapping(path = "/")
    public String main(Principal principal, Model model) {
        model.addAttribute("authentic", userService.isAuthenticates(principal));
        return "mainPage";
    }

    @GetMapping(path = "/fee")
    public String fee(@ModelAttribute("user") User user) {
        return "fee";
    }

    @GetMapping(path = "/contacts")
    public String getPersonal(Principal principal, Model model) {
        model.addAttribute("authentic", userService.isAuthenticates(principal));
        model.addAttribute("users", userService.findAllWithThesis());

        List<Publication> plenaryList = new ArrayList<>();
        Iterable<Publication> publicationList = publicationService.findAllPublications();
        for (Publication publication : publicationList) {
            if (publication.getPresentationType().equals(PresentationType.PLENARY.toString())) {
                plenaryList.add(publication);
            }
        }
        model.addAttribute("plenaryList", plenaryList);
        return "contacts";
    }

    @GetMapping(path = "/announcement")
    public ResponseEntity<Resource> downloadAnnouncement(String param) throws IOException {
        File file = new File(staticPath + "/First_Announcement_2024_rus.pdf");
        HttpHeaders headers = new HttpHeaders();
        InputStreamResource resource = new InputStreamResource(new FileInputStream(file));

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(file.length())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    @GetMapping(path = "/culturalProgram")
    public ResponseEntity<Resource> downloadCulturalProgram(String param) throws IOException {
        File file = new File(staticPath + "/Kulturnaya_programma.pdf");
        HttpHeaders headers = new HttpHeaders();
        InputStreamResource resource = new InputStreamResource(new FileInputStream(file));

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(file.length())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    @GetMapping(path = "/fee/downloadAkademgorodokMap")
    public ResponseEntity<Resource> downloadAkademgorodokMap(String param) throws IOException {
        File file = new File(staticPath + "/Akadem_map_2023.pdf");
        HttpHeaders headers = new HttpHeaders();
        InputStreamResource resource = new InputStreamResource(new FileInputStream(file));

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(file.length())
                .contentType(MediaType.APPLICATION_PDF)
                .body(resource);
    }
}
