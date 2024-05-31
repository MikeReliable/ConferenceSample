package com.mike.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mike.domain.*;
import com.mike.repos.AffiliationRepo;
import com.mike.repos.PaperRepo;
import com.mike.repos.UserRepo;
import com.mike.service.PublicationDTO;
import com.mike.service.UserDTO;
import com.mike.service.ControllerUtils;
import com.mike.service.PublicationService;
import com.mike.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.repository.query.Param;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class PersonalController {

    private UserService userService;

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Autowired
    private PaperRepo paperRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private AffiliationRepo affiliationRepo;

    @Autowired
    private PublicationService publicationService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${upload.path}")
    private String thesisUploadPath;

    @Value("${contracts.upload.path}")
    private String contractsUploadPath;

    @Value("${actual.year}")
    private int actualYear;

    @ModelAttribute("actualYear")
    public int actualYear() {
        return actualYear;
    }

    @ModelAttribute("actualYearBool")
    public boolean isActualYear(Principal principal) {
        User currentUser = userService.loadUserByUsername(principal.getName());
        return currentUser.getActualYear() != actualYear;
    }

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

    @ModelAttribute("secretary")
    public boolean isSecretary(Principal principal) {
        if (!userService.isAuthenticates(principal)) {
            User currentUser = userService.loadUserByUsername(principal.getName());
            return currentUser.getRoles().contains(Role.ADMIN) || currentUser.getRoles().contains(Role.SECRETARY);
        }
        return false;
    }

    @ModelAttribute("role")
    public Set<Role> roles(Principal principal) {
        if (!userService.isAuthenticates(principal)) {
            User user = userService.loadUserByUsername(principal.getName());
            return user.getRoles();
        }
        return null;
    }

    @ModelAttribute("personal")
    public boolean accessMenu() {
        return true;
    }

    @ModelAttribute("authentic")
    public boolean authentic(Principal principal) {
        return userService.isAuthenticates(principal);
    }

    @ModelAttribute("sections")
    public List<Section> sections(Principal principal) {
        if (!userService.isAuthenticates(principal)) {
            return Arrays.asList(Section.values());
        }
        return null;
    }

    @ModelAttribute("contractTypes")
    public List<ContractType> contractTypes(Principal principal) {
        if (!userService.isAuthenticates(principal)) {
            return Arrays.asList(ContractType.values());
        }
        return null;
    }

    @GetMapping(path = "/personal")
    public String getPersonal(Principal principal,
                              @ModelAttribute("userRoleName") String userRoleName,
                              @ModelAttribute("affiliation") Affiliation affiliation,
                              @ModelAttribute("publication") Publication publication,
                              @ModelAttribute("paper") Paper paper,
                              Model model) {
        Set<PresentationType> types = EnumSet.allOf(PresentationType.class);
        model.addAttribute("types", types);
        User currentUser = userService.loadUserByUsername(principal.getName());
        model.addAttribute("user", currentUser);
        model.addAttribute("young", currentUser.isYoung());
        model.addAttribute("publications", currentUser.getPublicationSet());
        model.addAttribute("papers", currentUser.getPaperSet());
        model.addAttribute("poster", PresentationType.POSTER);
        model.addAttribute("sectional", PresentationType.SECTION);
        List<Publication> publications = publicationService.getAllUserPublicationsSort(currentUser);
        List<PublicationDTO> publicationList = publicationService.getPublicationList(publications);
        model.addAttribute("publicationList", publicationList);

        boolean post = false;
        for (Publication p : currentUser.getPublicationSet()) {
            if (!post) {
                post = p.getPresentationType().equals(PresentationType.POSTER.getPresentationType());
            }
        }
        model.addAttribute("posterexist", post);

        return "personal";
    }

    @PostMapping(path = "/personalUpdate")
    public String personalUpdate(Principal principal,
                                 @ModelAttribute("userRoleName") String userRoleName,
                                 @ModelAttribute("active") String activation,
                                 @ModelAttribute("affiliation") Affiliation affiliation,
                                 @ModelAttribute("publication") Publication publication,
                                 @ModelAttribute("paper") Paper paper,
                                 @RequestParam String firstName,
                                 @RequestParam String middleName,
                                 @RequestParam String lastName,
                                 @RequestParam(value = "young", required = false, defaultValue = "false") boolean young,
                                 @RequestParam String degree,
                                 @RequestParam String position,
                                 @RequestParam String telephone,
                                 @ModelAttribute @Valid User currentUser,
                                 BindingResult result,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        currentUser = userService.loadUserByUsername(principal.getName());
        model.addAttribute("user", currentUser);
        model.addAttribute("young", currentUser.isYoung());
        model.addAttribute("publications", currentUser.getPublicationSet());
        model.addAttribute("papers", currentUser.getPaperSet());
        model.addAttribute("poster", PresentationType.POSTER);
        model.addAttribute("sectional", PresentationType.SECTION);
        List<Publication> publications = publicationService.getAllUserPublicationsSort(currentUser);
        List<PublicationDTO> publicationList = publicationService.getPublicationList(publications);
        model.addAttribute("publicationList", publicationList);
        boolean isFirstNameEmpty = StringUtils.isEmpty(firstName);
        boolean isLastNameEmpty = StringUtils.isEmpty(lastName);
        User userNameFromDb = userRepo.findByFirstNameAndMiddleNameAndLastName(currentUser.getFirstName(), currentUser.getMiddleName(), currentUser.getLastName());
        if (userNameFromDb != null && !userNameFromDb.equals(currentUser)) {
            model.addAttribute("message", "Пользователь с таким именем уже существует!");
            return "personal";
        }
        if (isFirstNameEmpty || isLastNameEmpty) {
            Map<String, String> errors = ControllerUtils.getErrors(result);
            System.out.println(errors);
            model.mergeAttributes(errors);
            model.addAttribute("message", "Изменения персональных данных не сохранены");
            return "personal";
        }
        userService.updatePersonal(currentUser, firstName, middleName, lastName, degree, position, telephone, young);
        redirectAttributes.addFlashAttribute("messageOk", "Новые регистрационные данные сохранены");
        return "redirect:/personal";
    }

    @PostMapping(path = "/personalPasswordEdit")
    public String personalPasswordEdit(Principal principal,
                                       @RequestParam("confirmation") String confirmation,
                                       @RequestParam("newPassword") String newPassword,
                                       @RequestParam("password") String password,
                                       @ModelAttribute("affiliation") Affiliation affiliation,
                                       @ModelAttribute("publication") Publication publication,
                                       @ModelAttribute("paper") Paper paper,
                                       @ModelAttribute("user") @Valid User currentUser,
                                       BindingResult result,
                                       Model model) {
        currentUser = userService.loadUserByUsername(principal.getName());
        model.addAttribute("user", currentUser);
        model.addAttribute("young", currentUser.isYoung());
        model.addAttribute("publications", currentUser.getPublicationSet());
        model.addAttribute("papers", currentUser.getPaperSet());
        model.addAttribute("poster", PresentationType.POSTER);
        model.addAttribute("sectional", PresentationType.SECTION);
        List<Publication> publications = publicationService.getAllUserPublicationsSort(currentUser);
        List<PublicationDTO> publicationList = publicationService.getPublicationList(publications);
        model.addAttribute("publicationList", publicationList);

        boolean isPasswordEmpty = StringUtils.isEmpty(newPassword);
        boolean isConfirmationEmpty = StringUtils.isEmpty(confirmation);

        if (!passwordEncoder.matches(password, currentUser.getPassword()) || isPasswordEmpty || isConfirmationEmpty) {
            if (!passwordEncoder.matches(password, currentUser.getPassword())) {
                model.addAttribute("message", "Изменения пароля не сохранены");
                model.addAttribute("passwordError", "Пароль не верный");
            }
            if (isConfirmationEmpty) {
                model.addAttribute("confirmation", "Поле не может быть пустым");
            }
            if (newPassword.length() < 6) {
                model.addAttribute("mess", "Пароль не менее 6 знаков");
            }
            if (isPasswordEmpty) {
                model.addAttribute("mess", "Поле не может быть пустым");
            }
            Map<String, String> errors = ControllerUtils.getErrors(result);
            System.out.println(errors);
            model.mergeAttributes(errors);
            model.addAttribute("message", "Изменения пароля не сохранены");
            return "personal";
        }
        if (!newPassword.equals(confirmation)) {
            model.addAttribute("mess", "Пароли не совпадают");
            model.addAttribute("message", "Изменения пароля не сохранены");
            return "personal";
        }

        currentUser.setPassword(passwordEncoder.encode(newPassword));
        userRepo.save(currentUser);
        model.addAttribute("messageOk", "Изменения пароля сохранены");
        return "personal";
    }

    @GetMapping(path = "/{user}/thesisAdd")
    public String getThesis(Principal principal,
                            @PathVariable User user,
                            @ModelAttribute("userRoleName") String userRoleName,
                            @ModelAttribute("affiliation") Affiliation affiliation,
                            @ModelAttribute("publication") Publication publication,
                            Model model) throws IOException {
        getAffiliationsScript(model);
        Set<PresentationType> types = EnumSet.allOf(PresentationType.class);
        model.addAttribute("types", types);
        User principalUser = userService.loadUserByUsername(principal.getName());
        User currentUser = userRepo.getById(user.getId());
        model.addAttribute("young", user.isYoung());
        model.addAttribute("publications", user.getPublicationSet());
        model.addAttribute("poster", PresentationType.POSTER);
        model.addAttribute("sectional", PresentationType.SECTION);
        List<Publication> publications = publicationService.getAllUserPublicationsSort(user);
        List<PublicationDTO> publicationList = publicationService.getPublicationList(publications);
        model.addAttribute("publicationList", publicationList);

        jsonUsersCoauthorsSet(model, user);
        if (principalUser.getRoles().contains(Role.ADMIN) ||
                principalUser.getRoles().contains(Role.SECRETARY) ||
                principalUser.getId().equals(currentUser.getId())) {
            return "thesis";
        } else {
            return "redirect:/personal";
        }
    }


    @PostMapping(path = "/{user}/thesisAdd")
    public String setThesis(Principal principal,
                            @PathVariable User user,
                            @ModelAttribute("publication") @Valid Publication publication,
                            BindingResult result,
                            Model model,
                            @ModelAttribute("section") String section,
                            @ModelAttribute("type") String type,
                            @RequestParam String publicationName,
                            @RequestParam MultipartFile file,
                            @RequestParam MultipartFile permissionFile,
                            @RequestParam String firstName0,
                            @RequestParam String middleName0,
                            @RequestParam String lastName0,
                            @RequestParam String organizationShort0,
                            @RequestParam(required = false) String firstName1,
                            @RequestParam(required = false) String middleName1,
                            @RequestParam(required = false) String lastName1,
                            @RequestParam(required = false) String organizationShort1,
                            @RequestParam(required = false) String firstName2,
                            @RequestParam(required = false) String middleName2,
                            @RequestParam(required = false) String lastName2,
                            @RequestParam(required = false) String organizationShort2,
                            @RequestParam(required = false) String firstName3,
                            @RequestParam(required = false) String middleName3,
                            @RequestParam(required = false) String lastName3,
                            @RequestParam(required = false) String organizationShort3,
                            @RequestParam(required = false) String firstName4,
                            @RequestParam(required = false) String middleName4,
                            @RequestParam(required = false) String lastName4,
                            @RequestParam(required = false) String organizationShort4,
                            @RequestParam(required = false) String firstName5,
                            @RequestParam(required = false) String middleName5,
                            @RequestParam(required = false) String lastName5,
                            @RequestParam(required = false) String organizationShort5,
                            @RequestParam(required = false) String firstName6,
                            @RequestParam(required = false) String middleName6,
                            @RequestParam(required = false) String lastName6,
                            @RequestParam(required = false) String organizationShort6,
                            @RequestParam(required = false) String firstName7,
                            @RequestParam(required = false) String middleName7,
                            @RequestParam(required = false) String lastName7,
                            @RequestParam(required = false) String organizationShort7,
                            @RequestParam(required = false) String firstName8,
                            @RequestParam(required = false) String middleName8,
                            @RequestParam(required = false) String lastName8,
                            @RequestParam(required = false) String organizationShort8,
                            @RequestParam(required = false) String firstName9,
                            @RequestParam(required = false) String middleName9,
                            @RequestParam(required = false) String lastName9,
                            @RequestParam(required = false) String organizationShort9,
                            RedirectAttributes redirectAttributes) throws IOException {
        model.addAttribute("flag", true);
        getAffiliationsScript(model);
        Set<PresentationType> types = EnumSet.allOf(PresentationType.class);
        model.addAttribute("types", types);
        model.addAttribute("young", user.isYoung());
        model.addAttribute("publications", user.getPublicationSet());
        model.addAttribute("poster", PresentationType.POSTER);
        model.addAttribute("sectional", PresentationType.SECTION);

        jsonUsersCoauthorsSet(model, user);

        if (result.hasErrors()) {
            Map<String, String> error = ControllerUtils.getErrors(result);
            model.mergeAttributes(error);
            model.addAttribute("message", "Ошибка загрузки тезисов");
            return "thesis";
        }
        if (publicationService.thesisExists(publication.getPublicationName())) {
            model.addAttribute("message", "Доклад с таким названием уже существует");
            return "thesis";
        }
        if (firstName0.equals("") || lastName0.equals("")) {
            model.addAttribute("message", "Авторы публикации не заполнены");
            return "thesis";
        }
        if (!publicationService.publicationSectionAndType(section, type)) {
            model.addAttribute("message", "Не выбрана секция и/или тип доклада");
            return "thesis";
        }

        String filename = "empty";
        String permission = "empty";
        String publNameCut;
        if (publication.getPublicationName().length() > 50) {
            publNameCut = publication.getPublicationName().substring(0, 49);
        } else {
            publNameCut = publication.getPublicationName();
        }
        if (publNameCut.contains("/")) {
            publNameCut = publNameCut.replace("/", "_");
        }
        if (file != null && permissionFile != null && !Objects.requireNonNull(file.getOriginalFilename()).isEmpty() && !Objects.requireNonNull(permissionFile.getOriginalFilename()).isEmpty()) {
            File uploadDir = new File(thesisUploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdir();
            }
            String orig = file.getOriginalFilename();
            if (orig.contains(".")) {
                String fileType = orig.substring(orig.lastIndexOf('.'));
                if (fileType.equals(".doc") || fileType.equals(".docx") || fileType.equals(".rtf")) {
                    filename = user.getLastName() + "_" + publNameCut + "_thesis" + fileType;
                    file.transferTo(new File(thesisUploadPath + "/" + filename));
                } else {
                    model.addAttribute("message", "Неверный формат файла");
                    return "thesis";
                }
            }
            String origPerm = permissionFile.getOriginalFilename();
            if (origPerm.contains(".")) {
                String fileType = origPerm.substring(origPerm.lastIndexOf('.'));
                if (!fileType.equals(".pdf")) {
                    model.addAttribute("message", "Неверный формат файла");
                    return "thesis";
                }
                permission = user.getLastName() + "_" + publNameCut + "_thesis" + "_permission" + fileType;
                permissionFile.transferTo(new File(thesisUploadPath + "/" + permission));
            }
        } else {
            model.addAttribute("message", "Файл тезисов  и/или разрешения на публикацию не выбран");
            return "thesis";
        }

        boolean test = publicationService.publicationSave(publicationName,
                section, type, user, publication, filename, permission,
                firstName0, middleName0, lastName0, organizationShort0,
                firstName1, middleName1, lastName1, organizationShort1,
                firstName2, middleName2, lastName2, organizationShort2,
                firstName3, middleName3, lastName3, organizationShort3,
                firstName4, middleName4, lastName4, organizationShort4,
                firstName5, middleName5, lastName5, organizationShort5,
                firstName6, middleName6, lastName6, organizationShort6,
                firstName7, middleName7, lastName7, organizationShort7,
                firstName8, middleName8, lastName8, organizationShort8,
                firstName9, middleName9, lastName9, organizationShort9);
        if (!test) {
            model.addAttribute("message", "Забыли внести себя в список авторов, либо не указали аффилиацию");
            return "thesis";
        }
        redirectAttributes.addFlashAttribute("messageOk", "Тезисы успешно загружены. Ожидайте сообщение о принятии на ваш email");
        return "redirect:/personal";
    }

    @GetMapping(path = "/{user}/thesisUpdate/{publicationId}")
    public String thesisGet(Principal principal,
                            @PathVariable User user,
                            @PathVariable Publication publicationId,
                            @ModelAttribute("userRoleName") String userRoleName,
                            @ModelAttribute("affiliation") Affiliation affiliation,
                            Model model) throws IOException {
        getAffiliationsScript(model);
        Set<PresentationType> types = EnumSet.allOf(PresentationType.class);
        model.addAttribute("types", types);
        User principalUser = userService.loadUserByUsername(principal.getName());
        User currentUser = userRepo.getById(user.getId());
        if (principalUser.getRoles().contains(Role.ADMIN) ||
                principalUser.getRoles().contains(Role.SECRETARY) ||
                principalUser.getId().equals(currentUser.getId())) {
            model.addAttribute("user", currentUser);
            model.addAttribute("young", currentUser.isYoung());
            model.addAttribute("publications", currentUser.getPublicationSet());
            model.addAttribute("poster", PresentationType.POSTER);
            model.addAttribute("sectional", PresentationType.SECTION);

            model.addAttribute("publication", publicationId);

            jsonUsersCoauthorsSet(model, currentUser);
            return "thesisRed";
        } else {
            return "redirect:/personal";
        }
    }

    @PostMapping(path = "/{user}/thesisUpdate/{publication}")
    public String thesisUpdate(Principal principal,
                               @PathVariable User user,
                               @PathVariable Publication publication,
                               Model model,
                               @RequestParam String section,
                               @RequestParam String type,
                               @RequestParam String publicationName,
                               @RequestParam MultipartFile file,
                               @RequestParam MultipartFile permissionFile,
                               @RequestParam String firstName0,
                               @RequestParam String middleName0,
                               @RequestParam String lastName0,
                               @RequestParam String organizationShort0,
                               @RequestParam(required = false) String firstName1,
                               @RequestParam(required = false) String middleName1,
                               @RequestParam(required = false) String lastName1,
                               @RequestParam(required = false) String organizationShort1,
                               @RequestParam(required = false) String firstName2,
                               @RequestParam(required = false) String middleName2,
                               @RequestParam(required = false) String lastName2,
                               @RequestParam(required = false) String organizationShort2,
                               @RequestParam(required = false) String firstName3,
                               @RequestParam(required = false) String middleName3,
                               @RequestParam(required = false) String lastName3,
                               @RequestParam(required = false) String organizationShort3,
                               @RequestParam(required = false) String firstName4,
                               @RequestParam(required = false) String middleName4,
                               @RequestParam(required = false) String lastName4,
                               @RequestParam(required = false) String organizationShort4,
                               @RequestParam(required = false) String firstName5,
                               @RequestParam(required = false) String middleName5,
                               @RequestParam(required = false) String lastName5,
                               @RequestParam(required = false) String organizationShort5,
                               @RequestParam(required = false) String firstName6,
                               @RequestParam(required = false) String middleName6,
                               @RequestParam(required = false) String lastName6,
                               @RequestParam(required = false) String organizationShort6,
                               @RequestParam(required = false) String firstName7,
                               @RequestParam(required = false) String middleName7,
                               @RequestParam(required = false) String lastName7,
                               @RequestParam(required = false) String organizationShort7,
                               @RequestParam(required = false) String firstName8,
                               @RequestParam(required = false) String middleName8,
                               @RequestParam(required = false) String lastName8,
                               @RequestParam(required = false) String organizationShort8,
                               @RequestParam(required = false) String firstName9,
                               @RequestParam(required = false) String middleName9,
                               @RequestParam(required = false) String lastName9,
                               @RequestParam(required = false) String organizationShort9,
                               RedirectAttributes redirectAttributes) throws IOException {
        model.addAttribute("flag", true);
        getAffiliationsScript(model);
        Set<PresentationType> types = EnumSet.allOf(PresentationType.class);
        model.addAttribute("types", types);
//        User currentUser = userRepo.getById(user.getId());
//        model.addAttribute("user", currentUser);
        model.addAttribute("young", user.isYoung());
        model.addAttribute("publications", user.getPublicationSet());
        model.addAttribute("poster", PresentationType.POSTER);
        model.addAttribute("sectional", PresentationType.SECTION);
        model.addAttribute("publication", publication);

        jsonUsersCoauthorsSet(model, user);
        Publication currentPublication = publicationService.findByPublicationId(publication.getPublicationId());

        if (publicationName.equals("")) {
            model.addAttribute("message", "Название не может быть пустым");
            return "thesisRed";
        }
        if (firstName0.equals("") || lastName0.equals("")) {
            model.addAttribute("message", "Авторы публикации не заполнены");
            return "thesisRed";
        }

        String filename = publication.getFilename();
        String permission = publication.getPermission();
        String publNameCut;
        if (publication.getPublicationName().length() > 50) {
            publNameCut = publication.getPublicationName().substring(0, 49);
        } else {
            publNameCut = publication.getPublicationName();
        }
        if (publNameCut.contains("/")) {
            publNameCut = publNameCut.replace("/", "_");
        }

        if (!file.getName().equals("") && !Objects.requireNonNull(file.getOriginalFilename()).isEmpty()) {
            File oldPublication = new File(thesisUploadPath + "/" + currentPublication.getFilename());
            oldPublication.delete();
            publication.setAcceptThesis(false);

            File uploadDir = new File(thesisUploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdir();
            }
            String orig = file.getOriginalFilename();
            if (orig.contains(".")) {
                String fileType = orig.substring(orig.lastIndexOf('.'));
                filename = user.getLastName() + "_" + publNameCut + "_thesis" + fileType;
                file.transferTo(new File(thesisUploadPath + "/" + filename));
            }
        }
        if (!permissionFile.getName().equals("") && !Objects.requireNonNull(permissionFile.getOriginalFilename()).isEmpty()) {
            File oldPermission = new File(thesisUploadPath + "/" + currentPublication.getPermission());
            oldPermission.delete();

            File uploadDir = new File(thesisUploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdir();
            }
            String orig = file.getOriginalFilename();
            String origPerm = permissionFile.getOriginalFilename();
            if (origPerm.contains(".")) {
                String fileType = origPerm.substring(origPerm.lastIndexOf('.'));
                permission = user.getLastName() + "_" + publNameCut + "_thesis" + "_permission" + fileType;
                permissionFile.transferTo(new File(thesisUploadPath + "/" + permission));
            }
        }
        boolean result = publicationService.publicationSave(publicationName,
                section, type, user,
                publication, filename, permission,
                firstName0, middleName0, lastName0, organizationShort0,
                firstName1, middleName1, lastName1, organizationShort1,
                firstName2, middleName2, lastName2, organizationShort2,
                firstName3, middleName3, lastName3, organizationShort3,
                firstName4, middleName4, lastName4, organizationShort4,
                firstName5, middleName5, lastName5, organizationShort5,
                firstName6, middleName6, lastName6, organizationShort6,
                firstName7, middleName7, lastName7, organizationShort7,
                firstName8, middleName8, lastName8, organizationShort8,
                firstName9, middleName9, lastName9, organizationShort9);
        if (!result) {
            model.addAttribute("message", "Забыли внести себя в список авторов");
            return "thesisRed";
        }

        redirectAttributes.addFlashAttribute("messageOk", "Тезисы успешно изменены. Ожидайте сообщение о принятии на ваш email");
        return "redirect:/personal";
    }

    @PostMapping(path = "/thesisPermissionUpdate")
    public String thesisPermissionUpdate(Principal principal,
                                         Model model,
                                         @Param(value = "id") Long id,
                                         @RequestParam MultipartFile permissionFile,
                                         RedirectAttributes redirectAttributes) throws IOException {
        Publication publication = publicationService.findByPublicationId(id);
        model.addAttribute("flag", true);
        getAffiliationsScript(model);
        Set<PresentationType> types = EnumSet.allOf(PresentationType.class);
        User user = userService.loadUserByUsername(principal.getName());
        model.addAttribute("types", types);
        model.addAttribute("young", user.isYoung());
        model.addAttribute("publications", user.getPublicationSet());
        model.addAttribute("poster", PresentationType.POSTER);
        model.addAttribute("sectional", PresentationType.SECTION);
        model.addAttribute("publication", publication);

        jsonUsersCoauthorsSet(model, user);
        Publication currentPublication = publicationService.findByPublicationId(publication.getPublicationId());

        String permission = publication.getPermission();
        String publNameCut;
        if (publication.getPublicationName().length() > 50) {
            publNameCut = publication.getPublicationName().substring(0, 49);
        } else {
            publNameCut = publication.getPublicationName();
        }
        if (publNameCut.contains("/")) {
            publNameCut = publNameCut.replace("/", "_");
        }

        if (permissionFile.getName().equals("") || Objects.requireNonNull(permissionFile.getOriginalFilename()).isEmpty()) {
            redirectAttributes.addFlashAttribute("message", "Не выбран файл разрешения на публикацию");
        } else {
            File oldPermission = new File(thesisUploadPath + "/" + currentPublication.getPermission());
            String origPerm = permissionFile.getOriginalFilename();
            if (origPerm.contains(".")) {
                String fileType = origPerm.substring(origPerm.lastIndexOf('.'));
                if (!fileType.equals(".pdf")) {
                    redirectAttributes.addFlashAttribute("message", "Неверный формат файла");
                } else {
                    oldPermission.delete();
                    permission = user.getLastName() + "_" + publNameCut + "_thesis" + "_permission" + fileType;
                    permissionFile.transferTo(new File(thesisUploadPath + "/" + permission));
                    publicationService.publicationPermissionSave(publication, permission);
                    redirectAttributes.addFlashAttribute("messageOk", "Разрешение на публикацию заменено");
                }
            }
        }
        return "redirect:/personal";
    }

    @PostMapping(path = "/publicationDelete")
    public String publicationDelete(@RequestParam("publicationId") Long id,
                                    RedirectAttributes redirectAttributes) {
        publicationService.publicationDelete(id);
        redirectAttributes.addFlashAttribute("messageOk", "Тезисы успешно удалены");
        return "redirect:/personal";
    }

    @PostMapping(path = "/publicationPosterDelete")
    public String publicationPosterDelete(@ModelAttribute("affiliation") Affiliation affiliation,
                                          @ModelAttribute("publication") Publication publication,
                                          @ModelAttribute("paper") Paper paper,
                                          @RequestParam("id") Long id) {

        Publication currentPublication = publicationService.findByPublicationId(id);
        String posterName = currentPublication.getPoster();
        File oldPoster = new File(thesisUploadPath + "/" + posterName);
        oldPoster.delete();
        currentPublication.setPoster(null);
        return "redirect:/personal";
    }

    @PostMapping(path = "/paperDelete")
    public String paperDelete(@ModelAttribute("affiliation") Affiliation affiliation,
                              @ModelAttribute("publication") Publication publication,
                              @ModelAttribute("paper") Paper paper,
                              @RequestParam("paperId") Long paperId,
                              RedirectAttributes redirectAttributes) {
        Paper currentPaper = publicationService.findByPaperId(paperId);
        String paperName = currentPaper.getPaperFilename();
        String paperNamePdf = currentPaper.getPaperFilenamePdf();
        String permissionName = currentPaper.getPaperPermission();
        String reviewName = currentPaper.getPaperReview();
        File oldPaper = new File(thesisUploadPath + "/" + paperName);
        File oldPaperPdf = new File(thesisUploadPath + "/" + paperNamePdf);
        File oldPermission = new File(thesisUploadPath + "/" + permissionName);
        File oldReview = new File(thesisUploadPath + "/" + reviewName);
        oldPaper.delete();
        oldPaperPdf.delete();
        oldPermission.delete();
        oldReview.delete();
        publicationService.paperDelete(paperId);
        redirectAttributes.addFlashAttribute("messageOk", "Статья успешно удалена");
        return "redirect:/personal";
    }

    @PostMapping(path = "/userContractDelete")
    public String userContractDelete(@ModelAttribute("affiliation") Affiliation affiliation,
                                     @ModelAttribute("publication") Publication publication,
                                     @ModelAttribute("paper") Paper paper,
                                     @RequestParam("id") Long id,
                                     RedirectAttributes redirectAttributes) {
        UserContract userContract = userService.findByContractId(id);
        String userContractFilename = userContract.getContractFilename();
        File oldPaper = new File(contractsUploadPath + "/" + userContractFilename);
        oldPaper.delete();
        userService.delete(userContract);
        redirectAttributes.addFlashAttribute("messageOk", "Договор на оргвзнос успешно удален");
        return "redirect:/personal";
    }

    @PostMapping(path = "/userContractCheckDelete")
    public String userContractCheckDelete(@ModelAttribute("affiliation") Affiliation affiliation,
                                          @ModelAttribute("publication") Publication publication,
                                          @ModelAttribute("paper") Paper paper,
                                          @RequestParam("id") Long id,
                                          RedirectAttributes redirectAttributes) {
        UserContract userContract = userService.findByContractId(id);
        String userContractCheckFilename = userContract.getCheckFilename();
        File oldPaper = new File(contractsUploadPath + "/" + userContractCheckFilename);
        oldPaper.delete();
        userContract.setCheckFilename(null);
        redirectAttributes.addFlashAttribute("messageOk", "Чек оплаты договора успешно удален");
        return "redirect:/personal";
    }

    @GetMapping("/publicationGet")
    @ResponseBody
    public FileSystemResource downloadFile(@Param(value = "id") Long id) {
        Publication currentPublication = publicationService.findByPublicationId(id);
        String filename = currentPublication.getFilename();
        String fileURL = thesisUploadPath + "/" + filename;
        return new FileSystemResource(new File(fileURL));
    }

    @GetMapping("/permissionGet")
    @ResponseBody
    public FileSystemResource downloadPermission(@Param(value = "id") Long id) {
        Publication currentPublication = publicationService.findByPublicationId(id);
        String filename = currentPublication.getPermission();
        String fileURL = thesisUploadPath + "/" + filename;
        return new FileSystemResource(new File(fileURL));
    }


    @GetMapping("/posterGet")
    @ResponseBody
    public FileSystemResource downloadPoster(@Param(value = "id") Long id) {
        Publication currentPublication = publicationService.findByPublicationId(id);
        String filename = currentPublication.getPoster();
        String fileURL = thesisUploadPath + "/" + filename;
        return new FileSystemResource(new File(fileURL));
    }

    @GetMapping("/paperGet")
    @ResponseBody
    public FileSystemResource downloadPaper(@Param(value = "id") Long id) {
        Paper currentPaper = publicationService.findByPaperId(id);
        String filename = currentPaper.getPaperFilename();
        String fileURL = thesisUploadPath + "/" + filename;
        return new FileSystemResource(new File(fileURL));
    }

    @GetMapping("/paperPdfGet")
    @ResponseBody
    public FileSystemResource downloadPaperPdf(@Param(value = "id") Long id) {
        Paper currentPaper = publicationService.findByPaperId(id);
        String filenamePdf = currentPaper.getPaperFilenamePdf();
        String fileURL = thesisUploadPath + "/" + filenamePdf;
        return new FileSystemResource(new File(fileURL));
    }

    @GetMapping("/paperReviewGet")
    @ResponseBody
    public FileSystemResource downloadPaperReview(@Param(value = "id") Long id) {
        Paper currentPaper = publicationService.findByPaperId(id);
        String filename = currentPaper.getPaperReview();
        String fileURL = thesisUploadPath + "/" + filename;
        return new FileSystemResource(new File(fileURL));
    }

    @GetMapping("/userContractGet")
    @ResponseBody
    public FileSystemResource downloadUserContract(@Param(value = "id") Long id) {
        UserContract userContract = userService.findByContractId(id);
        String filename = userContract.getContractFilename();
        String fileURL = contractsUploadPath + "/" + filename;
        return new FileSystemResource(new File(fileURL));
    }

    @GetMapping("/userInvoiceGet")
    @ResponseBody
    public FileSystemResource downloadUserContractInvoice(@Param(value = "id") Long id) {
        UserContract userContract = userService.findByContractId(id);
        String filename = userContract.getInvoiceFilename();
        String fileURL = contractsUploadPath + "/" + filename;
        return new FileSystemResource(new File(fileURL));
    }

    @GetMapping("/userCheckGet")
    @ResponseBody
    public FileSystemResource downloadUserContractCheck(@Param(value = "id") Long id) {
        UserContract userContract = userService.findByContractId(id);
        String filename = userContract.getCheckFilename();
        String fileURL = contractsUploadPath + "/" + filename;
        return new FileSystemResource(new File(fileURL));
    }

    @PostMapping(path = "/publicationPosterLoad")
    public String publicationPosterLoad(Principal principal,
                                        @ModelAttribute("affiliation") Affiliation affiliation,
                                        @ModelAttribute("publication") Publication publication,
                                        @ModelAttribute("paper") Paper paper,
                                        @RequestParam MultipartFile posterFile,
                                        @RequestParam("publicationId") Long id,
                                        RedirectAttributes redirectAttributes) throws IOException {
        User currentUser = userService.loadUserByUsername(principal.getName());
        Publication currentPublication = publicationService.findByPublicationId(id);

        String posterName = currentPublication.getPoster();
        String publNameCut;
        if (currentPublication.getPublicationName().length() > 50) {
            publNameCut = currentPublication.getPublicationName().substring(0, 49);
        } else {
            publNameCut = currentPublication.getPublicationName();
        }
        if (publNameCut.contains("/")) {
            publNameCut = publNameCut.replace("/", "_");
        }
        if (posterFile != null && !Objects.requireNonNull(posterFile.getOriginalFilename()).isEmpty()) {
            String orig = posterFile.getOriginalFilename();
            if (orig.contains(".")) {
                String fileType = orig.substring(orig.lastIndexOf('.'));
                posterName = currentUser.getLastName() + "_" + publNameCut + "_poster" + fileType;
                posterFile.transferTo(new File(thesisUploadPath + "/" + posterName));
            }
        } else {
            redirectAttributes.addFlashAttribute("message", "Файл постера не выбран");
            return "redirect:/personal";
        }
        publicationService.publicationPosterLoad(id, posterName);
        redirectAttributes.addFlashAttribute("messageOk", "Постер успешно загружен");
        return "redirect:/personal";
    }

    @PostMapping(path = "/personalPaperLoad")
    public String paperLoad(Principal principal,
                            @ModelAttribute("affiliation") Affiliation affiliation,
                            @ModelAttribute("publication") Publication publication,
                            @ModelAttribute("paper") @Valid Paper paper,
                            BindingResult result,
                            Model model,
                            @RequestParam MultipartFile file,
                            @RequestParam MultipartFile filePdf,
                            @RequestParam MultipartFile permissionFile,
                            RedirectAttributes redirectAttributes) throws IOException {
        Set<PresentationType> types = EnumSet.allOf(PresentationType.class);
        model.addAttribute("types", types);
        User currentUser = userService.loadUserByUsername(principal.getName());
        model.addAttribute("user", currentUser);
        model.addAttribute("young", currentUser.isYoung());
        model.addAttribute("publications", currentUser.getPublicationSet());
        model.addAttribute("papers", currentUser.getPaperSet());
        model.addAttribute("poster", PresentationType.POSTER);
        model.addAttribute("sectional", PresentationType.SECTION);
        List<Publication> publications = publicationService.getAllUserPublicationsSort(currentUser);
        List<PublicationDTO> publicationList = publicationService.getPublicationList(publications);
        model.addAttribute("publicationList", publicationList);
        if (result.hasErrors()) {
            Map<String, String> error = ControllerUtils.getErrors(result);
            model.mergeAttributes(error);
            model.addAttribute("message", "Ошибка загрузки статьи");
            return "personal";
        }
        Map<String, String> filenames;
        String permission = "empty";
        if (publicationService.paperExists(paper.getPaperName(), paper.getPaperId())) {
            model.addAttribute("message", "Статья с таким названием уже существует");
            return "personal";
        } else if (file != null && filePdf != null && permissionFile != null &&
                !Objects.requireNonNull(file.getOriginalFilename()).isEmpty() &&
                !Objects.requireNonNull(filePdf.getOriginalFilename()).isEmpty() &&
                !Objects.requireNonNull(permissionFile.getOriginalFilename()).isEmpty()) {
            File uploadDir = new File(thesisUploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdir();
            }
            filenames = publicationService.getPaperFilename(paper, file, filePdf, currentUser);
            if (filenames.containsKey("message")) {
                model.addAttribute("message", filenames.get("message"));
                return "personal";
            }
            permission = publicationService.getPaperPermissionFilename(paper, permissionFile, currentUser, permission, thesisUploadPath);
            publicationService.paperSave(paper, currentUser, filenames.get("file"), filenames.get("filePdf"), permission);
            redirectAttributes.addFlashAttribute("messageOk", "Статья успешно загружена. Ожидайте ответа рецензента на ваш email");
        } else {
            model.addAttribute("message", "Файл статьи и/или разрешения на публикацию не выбран");
            return "personal";
        }
        return "redirect:/personal";
    }

    @PostMapping(path = "/personalPaperLoadRpj")
    public String paperLoadRpj(Principal principal,
                               @ModelAttribute("affiliation") Affiliation affiliation,
                               @ModelAttribute("publication") Publication publication,
                               @ModelAttribute("paper") @Valid Paper paper,
                               BindingResult result,
                               Model model,
                               @RequestParam MultipartFile file,
                               @RequestParam MultipartFile permissionFile,
                               RedirectAttributes redirectAttributes) throws IOException {
        Set<PresentationType> types = EnumSet.allOf(PresentationType.class);
        model.addAttribute("types", types);
        User currentUser = userService.loadUserByUsername(principal.getName());
        model.addAttribute("user", currentUser);
        model.addAttribute("young", currentUser.isYoung());
        model.addAttribute("publications", currentUser.getPublicationSet());
        model.addAttribute("papers", currentUser.getPaperSet());
        model.addAttribute("poster", PresentationType.POSTER);
        model.addAttribute("sectional", PresentationType.SECTION);
        List<Publication> publications = publicationService.getAllUserPublicationsSort(currentUser);
        List<PublicationDTO> publicationList = publicationService.getPublicationList(publications);
        model.addAttribute("publicationList", publicationList);
        if (result.hasErrors()) {
            Map<String, String> error = ControllerUtils.getErrors(result);
            model.mergeAttributes(error);
            model.addAttribute("message", "Ошибка загрузки статьи");
            return "personal";
        }
        Map<String, String> filenames;
        String permission = "empty";
        if (publicationService.paperExists(paper.getPaperName(), paper.getPaperId())) {
            model.addAttribute("message", "Статья с таким названием уже существует");
            return "personal";
        } else if (file != null && permissionFile != null &&
                !Objects.requireNonNull(file.getOriginalFilename()).isEmpty() &&
                !Objects.requireNonNull(permissionFile.getOriginalFilename()).isEmpty()) {
            File uploadDir = new File(thesisUploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdir();
            }
            filenames = publicationService.getPaperFilenameRpj(paper, file, currentUser);
            if (filenames.containsKey("message")) {
                model.addAttribute("message", filenames.get("message"));
                return "personal";
            }
            permission = publicationService.getPaperPermissionFilenameRpj(paper, permissionFile, currentUser, permission, thesisUploadPath);
            publicationService.paperSaveRpj(paper, currentUser, filenames.get("file"), permission);
            redirectAttributes.addFlashAttribute("messageOk", "Статья успешно загружена. Ожидайте ответа рецензента на ваш email");
        } else {
            model.addAttribute("message", "Файл статьи и/или разрешения на публикацию не выбран");
            return "personal";
        }
        return "redirect:/personal";
    }

    @PostMapping(path = "/paperUpdate")
    public String paperUpdate(Principal principal,
                              @ModelAttribute("affiliation") Affiliation affiliation,
                              @ModelAttribute("publication") Publication publication,
                              @ModelAttribute("paper") @Valid Paper paper,
                              BindingResult result,
                              Model model,
                              @RequestParam("paperId") Long paperId,
                              @RequestParam("paperName") String paperName,
                              @RequestParam MultipartFile file,
                              RedirectAttributes redirectAttributes) throws IOException {
        Set<PresentationType> types = EnumSet.allOf(PresentationType.class);
        model.addAttribute("types", types);
        model.addAttribute("flag", true);
        User currentUser = userService.loadUserByUsername(principal.getName());
        model.addAttribute("user", currentUser);
        model.addAttribute("young", currentUser.isYoung());
        model.addAttribute("publications", currentUser.getPublicationSet());
        model.addAttribute("papers", currentUser.getPaperSet());
        model.addAttribute("poster", PresentationType.POSTER);
        model.addAttribute("sectional", PresentationType.SECTION);
        List<Publication> publications = publicationService.getAllUserPublicationsSort(currentUser);
        List<PublicationDTO> publicationList = publicationService.getPublicationList(publications);
        model.addAttribute("publicationList", publicationList);
        Paper currentPaper = publicationService.findByPaperId(paperId);
        if (result.hasErrors()) {
            Map<String, String> error = ControllerUtils.getErrors(result);
            model.mergeAttributes(error);
            model.addAttribute("message", "Ошибка загрузки статьи");
            return "personal";
        }
        if (publicationService.paperExists(currentPaper.getPaperName(), currentPaper.getPaperId())) {
            model.addAttribute("message", "Статья с таким названием уже существует");
            return "personal";
        }
        String filename = currentPaper.getPaperFilename();
        String filenamePdf = currentPaper.getPaperFilenamePdf();
        String permission = currentPaper.getPaperPermission();
        String paperNameCut = AdminController.nameCut(paper);
        if (file != null && !Objects.requireNonNull(file.getOriginalFilename()).isEmpty()) {

            String orig = file.getOriginalFilename();
            if (currentPaper.getPaperFilename().contains("paper")) {
                String fileType = orig.substring(orig.lastIndexOf('.'));
                String permissionType = permission.substring(permission.lastIndexOf('.'));
                File oldOne = new File(thesisUploadPath + "/" + filename);
                oldOne.delete();
                filename = currentUser.getLastName() + "_" + paperNameCut + "_paper" + fileType;
                file.transferTo(new File(thesisUploadPath + "/" + filename));

                filenamePdf = currentUser.getLastName() + "_" + paperNameCut + "_paper_AIP.pdf";
                permission = currentUser.getLastName() + "_" + paperNameCut + "_paper" + "_permission" + permissionType;
            }
            if (currentPaper.getPaperFilename().contains("RPJ")) {
                String fileType = orig.substring(orig.lastIndexOf('.'));
                String permissionType = permission.substring(permission.lastIndexOf('.'));
                File oldOne = new File(thesisUploadPath + "/" + filename);
                oldOne.delete();
                filename = currentUser.getLastName() + "_" + paperNameCut + "_RPJ" + fileType;
                file.transferTo(new File(thesisUploadPath + "/" + filename));
                permission = currentUser.getLastName() + "_" + paperNameCut + "_RPJ" + "_permission" + permissionType;
            }

            File filenamePdfOld = new File(thesisUploadPath + "/" + currentPaper.getPaperFilenamePdf());
            File permissionOld = new File(thesisUploadPath + "/" + currentPaper.getPaperPermission());

            File filenamePdfNew = new File(thesisUploadPath + "/" + filenamePdf);
            File permissionNew = new File(thesisUploadPath + "/" + permission);

            boolean successPdf = filenamePdfOld.renameTo(filenamePdfNew);
            boolean success = permissionOld.renameTo(permissionNew);
            System.out.println(success);
        } else {
            model.addAttribute("message", "Файл статьи не выбран");
            return "personal";
        }
        publicationService.paperUpdate(paperId, paperName, filename, filenamePdf, permission);
        redirectAttributes.addFlashAttribute("messageOk", "Статья успешно изменена. Ожидайте ответа рецензента на ваш email");
        return "redirect:/personal";
    }

    @PostMapping(path = "/paperUpdateCorrected")
    public String paperUpdateCorrected(Principal principal,
                                       @ModelAttribute("affiliation") Affiliation affiliation,
                                       @ModelAttribute("publication") Publication publication,
                                       @ModelAttribute("state") String state,
                                       @ModelAttribute("paper") Paper paper,
                                       @RequestParam("paperId") Long paperId,
                                       @RequestParam MultipartFile file,
                                       Model model,
                                       RedirectAttributes redirectAttributes) throws IOException {
        Paper currentPaper = publicationService.findByPaperId(paperId);
        User currentUser = userService.loadUserByUsername(principal.getName());
        model.addAttribute("user", currentUser);
        model.addAttribute("papers", currentUser.getPaperSet());
        model.addAttribute("poster", PresentationType.POSTER);
        model.addAttribute("sectional", PresentationType.SECTION);
        List<Publication> publications = publicationService.getAllUserPublicationsSort(currentUser);
        List<PublicationDTO> publicationList = publicationService.getPublicationList(publications);
        model.addAttribute("publicationList", publicationList);
        if (file != null && !Objects.requireNonNull(file.getOriginalFilename()).isEmpty()) {
            String orig = file.getOriginalFilename();
            String filename = currentPaper.getPaperFilename();
            String paperNameCut = AdminController.nameCut(currentPaper);
            if (orig.contains(".")) {
                String fileType = orig.substring(orig.lastIndexOf('.'));
                if (fileType.equals(".doc") || fileType.equals(".docx") || fileType.equals(".rtf")) {
                    File oldOne = new File(thesisUploadPath + "/" + filename);
                    oldOne.delete();
                    if (currentPaper.getPaperFilename().contains("paper")) {
                        filename = currentUser.getLastName() + "_" + paperNameCut + "_paper" + fileType;
                        file.transferTo(new File(thesisUploadPath + "/" + filename));
                    }
                    if (currentPaper.getPaperFilename().contains("RPJ")) {
                        filename = currentUser.getLastName() + "_" + paperNameCut + "_RPJ" + fileType;
                        file.transferTo(new File(thesisUploadPath + "/" + filename));
                    }
                } else {
                    model.addAttribute("message", "Выбран несоответствующий тип файла статьи");
                    return "personal";
                }
            }
            currentPaper.setState(PublicationState.CORRECTED);
            currentPaper.setPaperFilename(filename);
            paperRepo.save(currentPaper);
            redirectAttributes.addFlashAttribute("messageOk", "Статья успешно изменена. Ожидайте ответа рецензента на ваш email");
            return "redirect:/personal";
        } else {
            model.addAttribute("message", "Файл статьи с исправлениями не выбран");
        }
        return "personal";
    }

    @PostMapping(path = "/userContractLoad")
    public String userContractLoad(Principal principal,
                                   @ModelAttribute("affiliation") Affiliation affiliation,
                                   @ModelAttribute("publication") Publication publication,
                                   @ModelAttribute("paper") Paper paper,
                                   @ModelAttribute UserContract userContract,
                                   @ModelAttribute("contractType") String contractType,
                                   @RequestParam MultipartFile file,
                                   RedirectAttributes redirectAttributes,
                                   Model model) throws IOException {
        User currentUser = userService.loadUserByUsername(principal.getName());
        model.addAttribute("user", currentUser);
        model.addAttribute("papers", currentUser.getPaperSet());
        List<Publication> publications = publicationService.getAllUserPublicationsSort(currentUser);
        List<PublicationDTO> publicationList = publicationService.getPublicationList(publications);
        model.addAttribute("publicationList", publicationList);
        if (contractType.equals("")) {
            model.addAttribute("message", "Тип договора не выбран");
            return "personal";
        }
        if (!Objects.requireNonNull(file.getOriginalFilename()).isEmpty()) {
            userService.setContractFilename(file, currentUser, contractType);
            redirectAttributes.addFlashAttribute("messageOk", "Скан договора загружен. Ожидайте счет на оплату на ваш email");
            return "redirect:/personal";
        } else {
            model.addAttribute("message", "Файл договора не выбран");
        }
        return "personal";
    }

    @PostMapping(path = "/userCheckLoad")
    public String userCheckLoad(Principal principal,
                                @RequestParam("conrtactId") Long id,
                                @ModelAttribute("affiliation") Affiliation affiliation,
                                @ModelAttribute("publication") Publication publication,
                                @ModelAttribute("paper") Paper paper,
                                @ModelAttribute UserContract userContract,
                                @ModelAttribute("contractType") String contractType,
                                @RequestParam MultipartFile file,
                                RedirectAttributes redirectAttributes) throws IOException {
        User currentUser = userService.loadUserByUsername(principal.getName());
        if (file != null && !Objects.requireNonNull(file.getOriginalFilename()).isEmpty()) {
            userService.setContractCheckFilename(file, currentUser, id);
            redirectAttributes.addFlashAttribute("messageOk", "Чек об оплате успешно загружен");
        } else {
            redirectAttributes.addFlashAttribute("message", "Чек об оплате не выбран");
        }
        return "redirect:/personal";
    }

    @GetMapping(path = "/affiliation")
    public String getAffiliation(Principal principal,
                                 @ModelAttribute("userRoleName") String userRoleName,
                                 @ModelAttribute("affiliation") Affiliation affiliation,
                                 Model model) throws JsonProcessingException {
        getAffiliationsScript(model);
        User currentUser = userService.loadUserByUsername(principal.getName());
        model.addAttribute("user", currentUser);
        return "affiliation";
    }

    @PostMapping(path = "/affiliation")
    public String setAffiliation(Principal principal,
                                 @ModelAttribute("userRoleName") String userRoleName,
                                 @ModelAttribute("affiliation") @Valid Affiliation affiliation,
                                 BindingResult result,
                                 Model model,
                                 @ModelAttribute("publication") Publication publication,
                                 @RequestParam String organizationFull,
                                 @RequestParam String organizationShort,
                                 @RequestParam String city,
                                 @RequestParam String country,
                                 RedirectAttributes redirectAttributes) throws JsonProcessingException {
        getAffiliationsScript(model);
        Set<PresentationType> types = EnumSet.allOf(PresentationType.class);
        model.addAttribute("types", types);
        User currentUser = userService.loadUserByUsername(principal.getName());
        model.addAttribute("user", currentUser);
        model.addAttribute("young", currentUser.isYoung());
        model.addAttribute("publications", currentUser.getPublicationSet());
        model.addAttribute("poster", PresentationType.POSTER);
        model.addAttribute("sectional", PresentationType.SECTION);
        List<Publication> publications = publicationService.getAllUserPublicationsSort(currentUser);
        List<PublicationDTO> publicationList = publicationService.getPublicationList(publications);
        model.addAttribute("publicationList", publicationList);

        Map<String, String> messages = userService.affiliationSaveNew(result, organizationFull, organizationShort, city, country);
        if (messages.get("messageOk") == null) {
            model.addAttribute("message", messages.get("message"));
            model.addAttribute("organizationFull", messages.get("organizationFull"));
            model.addAttribute("organizationShort", messages.get("organizationShort"));
            model.addAttribute("city", messages.get("city"));
            model.addAttribute("country", messages.get("country"));
            return "affiliation";
        } else {
            redirectAttributes.addFlashAttribute("messageOk", messages.get("messageOk"));
            return "redirect:/thesisAdd";
        }
    }

    private void getAffiliationsScript(Model model) throws JsonProcessingException {
        Set<Affiliation> affiliationList = new HashSet<>(affiliationRepo.findAll());
        Set<AffiliationCut> affiliationSet = new HashSet<>();
        ObjectMapper mapper = new ObjectMapper();

        for (Affiliation affil : affiliationList) {
            AffiliationCut newAffil = new AffiliationCut();
            newAffil.setOrganizationFull(affil.getOrganizationFull());
            newAffil.setOrganizationShort(affil.getOrganizationShort());
            newAffil.setCity(affil.getCity());
            newAffil.setCountry(affil.getCountry());
            affiliationSet.add(newAffil);
        }
        List<Affiliation> affiliationListSorted = affiliationList.stream().sorted(Comparator.comparing(Affiliation::getOrganizationShort))
                .collect(Collectors.toList());
        List<AffiliationCut> affiliationCutListSorted = affiliationSet.stream().sorted(Comparator.comparing(AffiliationCut::getOrganizationShort))
                .collect(Collectors.toList());


        String jsonAffiliationCutList = mapper.writeValueAsString(affiliationCutListSorted);
        model.addAttribute("affiliationList", affiliationListSorted);
        model.addAttribute("jsonAffiliationSet", jsonAffiliationCutList);
    }

    private void jsonUsersCoauthorsSet(Model model, User currentUser) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        UserDTO userDTOCurrent = userService.setUserDTO(currentUser);

        String jsonCurrentUser = mapper.writeValueAsString(userDTOCurrent);
        model.addAttribute("jsonCurrentUser", jsonCurrentUser);

        Set<UserDTO> userDTOArray = userService.getUserDTOArray();
        String jsonString = mapper.writeValueAsString(userDTOArray);
        model.addAttribute("userCutArray", jsonString);
    }
}
