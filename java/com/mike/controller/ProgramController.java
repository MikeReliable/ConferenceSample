package com.mike.controller;

import com.mike.domain.*;
import com.mike.repos.PublicationRepo;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.Principal;
import java.util.*;
import java.util.function.Function;

@Controller
public class ProgramController {

    @Autowired
    private UserService userService;

    @Autowired
    private PublicationService publicationService;

    @Value("${upload.path}")
    private String thesisUploadPath;

    @Value("${static}")
    private String staticPath;

    @Value("${photo.upload.path}")
    private String photoUploadPath;

    @ModelAttribute("valid")
    public boolean isValid(Principal principal) {
        if (!userService.isAuthenticates(principal)) {
            User currentUser = userService.loadUserByUsername(principal.getName());
            return !currentUser.getRoles().contains(Role.USER);
        }
        return false;
    }

    @ModelAttribute("adminSecretary")
    public boolean isAdmin(Principal principal) {
        if (!userService.isAuthenticates(principal)) {
            User currentUser = userService.loadUserByUsername(principal.getName());
            return currentUser.getRoles().contains(Role.ADMIN) || currentUser.getRoles().contains(Role.SECRETARY);
        }
        return false;
    }

    @GetMapping(path = "/program")
    public String getPersonal(Principal principal, Model model) {
        model.addAttribute("authentic", userService.isAuthenticates(principal));
        model.addAttribute("users", userService.findAllWithThesis());

        List<Publication> plenaryList = new ArrayList<>();
        Iterable<Publication> publicationList = publicationService.findAllPublications();
        for (Publication publication : publicationList) {
            if (publication.getPresentationType().equals(PresentationType.PLENARY.getPresentationType())) {
                plenaryList.add(publication);
            }
        }
        Collections.sort(plenaryList);
        model.addAttribute("plenaryList", plenaryList);
        model.addAttribute("photoUploadPath", photoUploadPath);
        return "program";
    }

    @GetMapping(path = "/posters")
    public String getPosters(Principal principal, Model model) {
        model.addAttribute("authentic", userService.isAuthenticates(principal));
        model.addAttribute("users", userService.findAllWithThesis());

        List<Publication> posterList = new ArrayList<>();
        Iterable<Publication> publicationList = publicationService.findAllPublications();
        for (Publication publication : publicationList) {
            if (publication.getPoster() != null) {
                posterList.add(publication);
            }
        }
        Collections.sort(posterList);
        model.addAttribute("posterList", posterList);
        return "posters";
    }

    @GetMapping(path = "/poster/{id}")
    public ResponseEntity<Resource> download(@PathVariable Publication id,
                                             String param) throws IOException {
        File file = new File(thesisUploadPath + "/" + id.getPoster());
        HttpHeaders headers = new HttpHeaders();
        InputStreamResource resource = new InputStreamResource(new FileInputStream(file));

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(file.length())
                .contentType(MediaType.APPLICATION_PDF)
                .body(resource);
    }

    // for replace the photo need to restart the server
    @PostMapping(path = "/program/plenaryFotoLoad")
    public String plenaryFotoLoad(@RequestParam("publicationId") Long publicationId,
                                  @RequestParam MultipartFile foto) throws IOException {
        if (foto != null && !Objects.requireNonNull(foto.getOriginalFilename()).isEmpty()) {
            String orig = foto.getOriginalFilename();
            if (orig.contains(".")) {
                File uploadDir = new File(photoUploadPath);
                if (!uploadDir.exists()) {
                    uploadDir.mkdir();
                }
                Publication publication = publicationService.findByPublicationId(publicationId);
                String fileType = orig.substring(orig.lastIndexOf('.'));
                String filename = publication.getUser().getLastName() + "_foto" + fileType;
                foto.transferTo(new File(photoUploadPath + "/" + filename));
                publication.getUser().setFoto(filename);
            }
        }
        return "redirect:/program";
    }

    @PostMapping(path = "/program/plenaryFotoDelete")
    public String plenaryFotoDelete(@RequestParam("publicationId") Long publicationId
    ) {
        Publication publication = publicationService.findByPublicationId(publicationId);
        String filename = publication.getUser().getFoto();
        File oldFilename = new File(photoUploadPath + "/" + filename);
        oldFilename.delete();
        publication.getUser().setFoto(null);
        return "redirect:/program";
    }

    @GetMapping(path = "/program/downloadProgram")
    public ResponseEntity<Resource> downloadProgram(String param) throws IOException {
        File file = new File(staticPath + "/Program_Mesomechanics_2022.pdf");
        HttpHeaders headers = new HttpHeaders();
        InputStreamResource resource = new InputStreamResource(new FileInputStream(file));

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(file.length())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    @GetMapping(path = "/program/downloadCurrentProgram")
    public ResponseEntity<Resource> downloadCurrentProgram(String param) throws IOException {
        File file = new File(staticPath + "/Program_Mesomechanics_2023.pdf");
        HttpHeaders headers = new HttpHeaders();
        InputStreamResource resource = new InputStreamResource(new FileInputStream(file));

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(file.length())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    @GetMapping(path = "/program/downloadCurrentThesis")
    public ResponseEntity<Resource> downloadCurrentThesis(String param) throws IOException {
        File file = new File(staticPath + "/Thesis_Mesomechanics_2023.pdf");
        HttpHeaders headers = new HttpHeaders();
        InputStreamResource resource = new InputStreamResource(new FileInputStream(file));

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(file.length())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    @GetMapping(path = "/program/downloadCrashCourse")
    public ResponseEntity<Resource> downloadCrashCourse(String param) throws IOException {
        File file = new File(staticPath + "/AM_Korsunskiy_Crash_course.pdf");
        HttpHeaders headers = new HttpHeaders();
        InputStreamResource resource = new InputStreamResource(new FileInputStream(file));

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(file.length())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }
}
