package com.mike.controller;

import com.mike.domain.Role;
import com.mike.domain.User;
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
import org.springframework.web.bind.annotation.PathVariable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.Principal;

@Controller
public class PublicationController {

    @Autowired
    private UserService userService;

    @Value("${static}")
    private String staticPath;

    @Value("${doi.path}")
    private String thesisByDoi;

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

    @GetMapping(path = "/publications")
    public String publications(Principal principal, Model model) {
        model.addAttribute("authentic", userService.isAuthenticates(principal));
        return "publications";
    }

    @GetMapping(path = "/publications/thesis")
    public ResponseEntity<Resource> download(String param) throws IOException {
        File file = new File(staticPath + "/Sample_thesis_2024.docx");
        HttpHeaders headers = new HttpHeaders();
        InputStreamResource resource = new InputStreamResource(new FileInputStream(file));

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(file.length())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    @GetMapping(path = "/publications/rpj")
    public ResponseEntity<Resource> downloadRpj(String param) throws IOException {
        File file = new File(staticPath + "/Rules_Russian_physics_journal.docx");
        HttpHeaders headers = new HttpHeaders();
        InputStreamResource resource = new InputStreamResource(new FileInputStream(file));

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(file.length())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    @GetMapping(path = "/publications/poster")
    public ResponseEntity<Resource> downloadPoster(String param) throws IOException {
        File file = new File(staticPath + "/Sample_poster_2023.pptx");
        HttpHeaders headers = new HttpHeaders();
        InputStreamResource resource = new InputStreamResource(new FileInputStream(file));

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(file.length())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    @GetMapping(path = "/publications/paper")
    public ResponseEntity<Resource> downloadPaper(String param) throws IOException {
        File file = new File(staticPath + "/AIP_Article_Template.docx");
        HttpHeaders headers = new HttpHeaders();
        InputStreamResource resource = new InputStreamResource(new FileInputStream(file));

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(file.length())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    @GetMapping(path = "/publications/license")
    public ResponseEntity<Resource> downloadLicense(String param) throws IOException {
        File file = new File(staticPath + "/AIP_Conference_Proceedings_License_Agreement.pdf");
        HttpHeaders headers = new HttpHeaders();
        InputStreamResource resource = new InputStreamResource(new FileInputStream(file));

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(file.length())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    @GetMapping(path = "/publications/licenseExample")
    public ResponseEntity<Resource> downloadLicenseExample(String param) throws IOException {
        File file = new File(staticPath + "/AIP_Conference_Proceedings_License_Agreement_example.pdf");
        HttpHeaders headers = new HttpHeaders();
        InputStreamResource resource = new InputStreamResource(new FileInputStream(file));

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(file.length())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    @GetMapping(path = "/publications/contractPersonal")
    public ResponseEntity<Resource> contractPersonal(String param) throws IOException {
        File file = new File(staticPath + "/ФЛ участие без публикации_2023.doc");
        HttpHeaders headers = new HttpHeaders();
        InputStreamResource resource = new InputStreamResource(new FileInputStream(file));

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(file.length())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    @GetMapping(path = "/publications/contractPersonalPaper")
    public ResponseEntity<Resource> contractPersonalPaper(String param) throws IOException {
        File file = new File(staticPath + "/ФЛ участие с публикацией Russian physics journal_2023.doc");
        HttpHeaders headers = new HttpHeaders();
        InputStreamResource resource = new InputStreamResource(new FileInputStream(file));

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(file.length())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    @GetMapping(path = "/publications/contractPersonalPaperRPJ")
    public ResponseEntity<Resource> contractPersonalPaperRPJ(String param) throws IOException {
        File file = new File(staticPath + "/ФЛ только публикация Russian physics journal без участия_2023.docx");
        HttpHeaders headers = new HttpHeaders();
        InputStreamResource resource = new InputStreamResource(new FileInputStream(file));

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(file.length())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    @GetMapping(path = "/publications/contractCompany")
    public ResponseEntity<Resource> contractCompany(String param) throws IOException {
        File file = new File(staticPath + "/ЮЛ участие без публикации_2023.doc");
        HttpHeaders headers = new HttpHeaders();
        InputStreamResource resource = new InputStreamResource(new FileInputStream(file));

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(file.length())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    @GetMapping(path = "/publications/contractCompanyPaper")
    public ResponseEntity<Resource> contractCompanyPaper(String param) throws IOException {
        File file = new File(staticPath + "/ЮЛ участие с публикацией Russian physics journal_2023.doc");
        HttpHeaders headers = new HttpHeaders();
        InputStreamResource resource = new InputStreamResource(new FileInputStream(file));

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(file.length())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    @GetMapping(path = "/publications/contractCompanyPaperRPJ")
    public ResponseEntity<Resource> contractCompanyPaperRPJ(String param) throws IOException {
        File file = new File(staticPath + "/ЮЛ только публикация Russian physics journal_без участия_2023.docx");
        HttpHeaders headers = new HttpHeaders();
        InputStreamResource resource = new InputStreamResource(new FileInputStream(file));

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(file.length())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    @GetMapping(path = "/publications/plenaryEn")
    public ResponseEntity<Resource> downloadPlenaryEn(String param) throws IOException {
        File file = new File(staticPath + "/Plenary_sessions_program.pdf");
        HttpHeaders headers = new HttpHeaders();
        InputStreamResource resource = new InputStreamResource(new FileInputStream(file));

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(file.length())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    @GetMapping(path = "/thesis/{doi}")
    public ResponseEntity<Resource> downloadThesisByDoi(@PathVariable String doi) throws IOException {
        File file = new File(thesisByDoi + "/thesis" + doi + ".pdf");
        HttpHeaders headers = new HttpHeaders();
        InputStreamResource resource = new InputStreamResource(new FileInputStream(file));

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(file.length())
                .contentType(MediaType.APPLICATION_PDF)
                .body(resource);
    }
}
