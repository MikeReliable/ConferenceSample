package com.mike.controller;

import com.mike.domain.*;
import com.mike.repos.*;
import com.mike.service.PublicationDTO;
import com.mike.service.ControllerUtils;
import com.mike.service.MailSenderService;
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

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    PublicationRepo publicationRepo;

    @Autowired
    AffiliationRepo affiliationRepo;

    @Autowired
    PaperRepo paperRepo;

    @Autowired
    UserRepo userRepo;

    @Autowired
    CoauthorRepo coauthorRepo;

    @Autowired
    PublicationCoauthorsRepo publicationCoauthorsRepo;


    @Autowired
    UserContractRepo userContractRepo;

    @Autowired
    private MailSenderService mailSenderService;

    @Value("${actual.year}")
    private int actualYear;

    private UserService userService;

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Autowired
    private PublicationService publicationService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${upload.path}")
    private String thesisUploadPath;

    @Value("${contracts.upload.path}")
    private String contractsUploadPath;

    @ModelAttribute("role")
    public Set<Role> roles(Principal principal) {
        if (!userService.isAuthenticates(principal)) {
            User user = userService.loadUserByUsername(principal.getName());
            return user.getRoles();
        }
        return null;
    }

    @ModelAttribute("sections")
    public List<Section> sections(Principal principal) {
        if (!userService.isAuthenticates(principal)) {
            return Arrays.asList(Section.values());
        }
        return null;
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

    @ModelAttribute("roles")
    public List<Role> roleList() {
        return Arrays.asList(Role.values());
    }


    @ModelAttribute("editor")
    public boolean isEditor(Principal principal) {
        if (!userService.isAuthenticates(principal)) {
            User currentUser = userService.loadUserByUsername(principal.getName());
            Set<Role> roles = new HashSet<>();
            roles.add(Role.EDITOR);
            roles.add(Role.FINANCIER);
            return currentUser.getRoles().stream().anyMatch(roles::contains);
        }
        return false;
    }

    @ModelAttribute("authentic")
    public boolean authentic(Principal principal) {
        return userService.isAuthenticates(principal);
    }

    @ModelAttribute("personal")
    public boolean accessMenu() {
        return false;
    }

    @GetMapping("/")
    public String adminMenu(Principal principal,
                            Model model) {
        if (!userService.isAuthenticates(principal)) {
            List<User> plenaryList = new ArrayList<>();
            Iterable<User> userList = userService.findAll();
            for (User usr : userList) {
                Set<Publication> publicationSet = usr.getPublicationSet();
                for (Publication publication : publicationSet) {
                    if (publication.getPresentationType().equals(PresentationType.PLENARY.getPresentationType())) {
                        plenaryList.add(usr);
                    }
                }
            }
            plenaryList.sort(Comparator.comparing(User::getLastName));
            model.addAttribute("plenaryList", plenaryList);
            getAllPublicationsSort(model);
            return "adminPage";
        }
        return "mainPage";
    }

    @GetMapping("/usersNoPublication")
    public String usersNoPublication(Principal principal,
                                     Model model) {
        if (!userService.isAuthenticates(principal)) {
            List<User> users = new ArrayList<>();
            List<User> usersOld = new ArrayList<>();
            List<User> usersDis = new ArrayList<>();
            List<User> usersBlocked = new ArrayList<>();
            List<User> userList = (List<User>) userService.findAll();
//            userList.removeIf(user -> !user.isActive());
            for (User usr : userList) {
                if (usr.getPublicationSet().isEmpty() && usr.isActive() && usr.isAccountNonLocked() && (usr.getActualYear() == actualYear)) {
                    users.add(usr);
                }
                if (usr.getActualYear() != actualYear && usr.isActive() && usr.isAccountNonLocked()) {
                    usersOld.add(usr);
                }
                if (!usr.isActive() && usr.isAccountNonLocked()) {
                    usersDis.add(usr);
                }
                if (!usr.isAccountNonLocked()) {
                    usersBlocked.add(usr);
                }
            }
            users.sort(Comparator.comparing(User::getLastName));
            usersOld.sort(Comparator.comparing(User::getLastName));
            usersDis.sort(Comparator.comparing(User::getLastName));
            usersBlocked.sort(Comparator.comparing(User::getLastName));
            model.addAttribute("users", users);
            model.addAttribute("usersOld", usersOld);
            model.addAttribute("usersDis", usersDis);
            model.addAttribute("usersBlocked", usersBlocked);
            return "adminNoPublication";
        }
        return "mainPage";
    }

    @GetMapping("/adminRedUser/{user}")
    public String adminRedUser(Principal principal,
                               @PathVariable User user,
                               @ModelAttribute("userRoleName") String userRoleName,
                               @ModelAttribute("paper") Paper paper,
                               Model model) {
        if (!userService.isAuthenticates(principal)) {
            getAffiliationsScript(model);
            model.addAttribute("user", user);
            model.addAttribute("young", user.isYoung());
            model.addAttribute("publications", user.getPublicationSet());
            model.addAttribute("papers", user.getPaperSet());
            Set<PresentationType> types = EnumSet.allOf(PresentationType.class);
            model.addAttribute("types", types);
            model.addAttribute("poster", PresentationType.POSTER.getPresentationType());
            List<Publication> publications = publicationService.getAllUserPublicationsSort(user);
            List<PublicationDTO> publicationList = publicationService.getPublicationList(publications);
            model.addAttribute("publicationList", publicationList);
            return "adminRedUser";
        }
        return "mainPage";
    }

    @PostMapping(path = "/adminRedUser/{user}/personalUpdate")
    public String adminUpdatePersonal(@ModelAttribute("userRoleName") String userRoleName,
                                      @ModelAttribute("active") String active,
                                      @ModelAttribute("block") String block,
                                      @RequestParam(value = "young", required = false, defaultValue = "false") boolean young,
                                      @ModelAttribute("paper") Paper paper,
                                      @ModelAttribute("user") @Valid User user,
                                      BindingResult result,
                                      Model model) {
        user = userService.loadUserByUsername(user.getUsername());
        model.addAttribute("user", user);
        model.addAttribute("young", user.isYoung());
        model.addAttribute("publications", user.getPublicationSet());
        model.addAttribute("papers", user.getPaperSet());
        Set<PresentationType> types = EnumSet.allOf(PresentationType.class);
        model.addAttribute("types", types);
        model.addAttribute("poster", PresentationType.POSTER.getPresentationType());
        List<Publication> publications = publicationService.getAllUserPublicationsSort(user);
        List<PublicationDTO> publicationList = publicationService.getPublicationList(publications);
        model.addAttribute("publicationList", publicationList);
        boolean isFirstNameEmpty = StringUtils.isEmpty(user.getFirstName());
        boolean isLastNameEmpty = StringUtils.isEmpty(user.getLastName());

        if (isFirstNameEmpty || isLastNameEmpty || result.hasErrors()) {
            Map<String, String> errors = ControllerUtils.getErrors(result);
            System.out.println(errors);
            model.mergeAttributes(errors);
            model.addAttribute("message", "Изменения персональных данных не сохранены");
            return "adminRedUser";
        }
        if (!userRoleName.equals("0")) {
            Set<Role> roleSet = new HashSet<>();
            switch (userRoleName) {
                case ("1"):
                    roleSet.add(Role.ADMIN);
                    break;
                case ("2"):
                    roleSet.add(Role.EDITOR);
                    break;
                case ("3"):
                    roleSet.add(Role.FINANCIER);
                    break;
                case ("4"):
                    roleSet.add(Role.SECRETARY);
                    break;
                default:
                    roleSet.add(Role.USER);
                    break;
            }
            user.setRoles(roleSet);
        }

        if (!block.equals("0")) {
            switch (block) {
                case ("1"):
                    user.setBlocked(false);
                    break;
                case ("2"):
                    user.setBlocked(true);
                    break;
            }
        }
        if (!active.equals("0")) {
            switch (active) {
                case ("1"):
                    user.setActive(true);
                    break;
                case ("2"):
                    user.setActive(false);
                    break;
            }
        }
        Coauthor coauthor = coauthorRepo.findByFirstNameAndMiddleNameAndLastName(user.getFirstName(), user.getMiddleName(), user.getLastName());
        if (coauthor != null) {
            coauthor.setFirstName(user.getFirstName());
            coauthor.setMiddleName(user.getMiddleName());
            coauthor.setLastName(user.getLastName());
        }
        user.setFirstName(user.getFirstName());
        user.setMiddleName(user.getMiddleName());
        user.setLastName(user.getLastName());
        user.setEmail(user.getEmail());
//        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setDegree(user.getDegree());
        user.setPosition(user.getPosition());
        user.setYoung(young);
        user.setTelephone(user.getTelephone());
        userRepo.save(user);
        if (coauthor != null) {
            coauthorRepo.save(coauthor);
        }
        model.addAttribute("messageOk", "Новые регистрационные данные сохранены");
        return "adminRedUser";
    }

    @PostMapping(path = "/adminRedUser/{user}/personalPasswordEdit")
    public String adminPersonalPasswordChange(@RequestParam("confirmation") String confirmation,
                                              @RequestParam("newPassword") String newPassword,
                                              Principal principal,
                                              @ModelAttribute("paper") Paper paper,
                                              @ModelAttribute("user") @Valid User user,
                                              BindingResult result,
                                              Model model) {
        getAffiliationsScript(model);
        model.addAttribute("user", user);
        model.addAttribute("young", user.isYoung());
        model.addAttribute("publications", user.getPublicationSet());
        model.addAttribute("papers", user.getPaperSet());
        Set<PresentationType> types = EnumSet.allOf(PresentationType.class);
        model.addAttribute("types", types);
        model.addAttribute("poster", PresentationType.POSTER.getPresentationType());
        List<Publication> publications = publicationService.getAllUserPublicationsSort(user);
        List<PublicationDTO> publicationList = publicationService.getPublicationList(publications);
        model.addAttribute("publicationList", publicationList);
        boolean isPasswordEmpty = StringUtils.isEmpty(newPassword);
        boolean isConfirmationEmpty = StringUtils.isEmpty(confirmation);
        if (isPasswordEmpty || isConfirmationEmpty) {
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
            return "adminRedUser";
        }

        if (!newPassword.equals(confirmation)) {
            model.addAttribute("mess", "Пароли не совпадают");
            model.addAttribute("message", "Изменения пароля не сохранены");
            return "adminRedUser";
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        model.addAttribute("messageOk", "Изменения пароля сохранены");
        return "adminRedUser";
    }

    @PostMapping(path = "/adminRedUser/{user}/publicationPosterLoad")
    public String publicationPosterLoad(@PathVariable User user,
                                        @ModelAttribute("paper") Paper paper,
                                        @RequestParam MultipartFile posterFile,
                                        @RequestParam("publicationId") Long id,
                                        RedirectAttributes redirectAttributes,
                                        Model model) throws IOException {
        getAffiliationsScript(model);
        model.addAttribute("user", user);
        model.addAttribute("young", user.isYoung());
        model.addAttribute("publications", user.getPublicationSet());
        model.addAttribute("papers", user.getPaperSet());
        Set<PresentationType> types = EnumSet.allOf(PresentationType.class);
        model.addAttribute("types", types);
        model.addAttribute("poster", PresentationType.POSTER.getPresentationType());
        List<Publication> publications = publicationService.getAllUserPublicationsSort(user);
        List<PublicationDTO> publicationList = publicationService.getPublicationList(publications);
        model.addAttribute("publicationList", publicationList);
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
                posterName = user.getLastName() + "_" + publNameCut + "_poster" + fileType;
                posterFile.transferTo(new File(thesisUploadPath + "/" + posterName));
            }
        } else {
            model.addAttribute("message", "Файл постера не выбран");
            return "adminRedUser";
        }
        publicationService.publicationPosterLoad(id, posterName);
        model.addAttribute("messageOk", "Постер успешно загружен");
        return "adminRedUser";
    }

    @PostMapping(path = "/adminRedUser/{user}/adminPaperLoad")
    public String adminPaperLoad(@PathVariable User user,
                                 @ModelAttribute("affiliation") Affiliation affiliation,
                                 @ModelAttribute("publication") Publication publication,
                                 @ModelAttribute("paper") @Valid Paper paper,
                                 BindingResult result,
                                 Model model,
                                 @RequestParam MultipartFile file,
                                 @RequestParam MultipartFile filePdf,
                                 @RequestParam MultipartFile permissionFile) throws IOException {
        getAffiliationsScript(model);
        model.addAttribute("user", user);
        model.addAttribute("young", user.isYoung());
        model.addAttribute("publications", user.getPublicationSet());
        model.addAttribute("papers", user.getPaperSet());
        model.addAttribute("poster", PresentationType.POSTER.getPresentationType());
        model.addAttribute("sectional", PresentationType.SECTION);
        List<Publication> publications = publicationService.getAllUserPublicationsSort(user);
        List<PublicationDTO> publicationList = publicationService.getPublicationList(publications);
        model.addAttribute("publicationList", publicationList);
        if (result.hasErrors()) {
            Map<String, String> error = ControllerUtils.getErrors(result);
            model.mergeAttributes(error);
            model.addAttribute("message", "Ошибка загрузки статьи");
            return "personal";
        }
        Map<String, String> filenames = new HashMap<>();
        String permission = "empty";
        if (file != null && permissionFile != null && !Objects.requireNonNull(file.getOriginalFilename()).isEmpty() && !Objects.requireNonNull(permissionFile.getOriginalFilename()).isEmpty()) {
            File uploadDir = new File(thesisUploadPath);
            filenames = publicationService.getPaperFilename(paper, file, filePdf, user);
            permission = publicationService.getPaperPermissionFilename(paper, permissionFile, user, permission, thesisUploadPath);
        } else {
            model.addAttribute("message", "Файл тезисов  и/или разрешения на публикацию не выбран");
            return "personal";
        }

        if (publicationService.paperExists(paper.getPaperName(), paper.getPaperId())) {
            model.addAttribute("message", "Статья с таким названием уже существует");
            return "personal";
        }
        publicationService.paperSave(paper, user, filenames.get("file"), filenames.get("filePdf"), permission);
        model.addAttribute("messageOk", "Статья успешно загружена. Ожидайте ответа рецензента на ваш email");
        return "personal";
    }

    @PostMapping(path = "/adminRedUser/{user}/adminPaperLoadRpj")
    public String adminPaperLoadRpj(@PathVariable User user,
                                    @ModelAttribute("affiliation") Affiliation affiliation,
                                    @ModelAttribute("publication") Publication publication,
                                    @ModelAttribute("paper") @Valid Paper paper,
                                    BindingResult result,
                                    Model model,
                                    @RequestParam MultipartFile file,
                                    @RequestParam MultipartFile permissionFile,
                                    RedirectAttributes redirectAttributes) throws IOException {
        getAffiliationsScript(model);
        model.addAttribute("user", user);
        model.addAttribute("young", user.isYoung());
        model.addAttribute("publications", user.getPublicationSet());
        model.addAttribute("papers", user.getPaperSet());
        model.addAttribute("poster", PresentationType.POSTER.getPresentationType());
        model.addAttribute("sectional", PresentationType.SECTION);
        List<Publication> publications = publicationService.getAllUserPublicationsSort(user);
        List<PublicationDTO> publicationList = publicationService.getPublicationList(publications);
        model.addAttribute("publicationList", publicationList);
        if (result.hasErrors()) {
            Map<String, String> error = ControllerUtils.getErrors(result);
            model.mergeAttributes(error);
            model.addAttribute("message", "Ошибка загрузки статьи");
            return "personal";
        }
        Map<String, String> filenames = new HashMap<>();
        String permission = "empty";
        if (file != null && permissionFile != null && !Objects.requireNonNull(file.getOriginalFilename()).isEmpty() && !Objects.requireNonNull(permissionFile.getOriginalFilename()).isEmpty()) {
            File uploadDir = new File(thesisUploadPath);
            filenames = publicationService.getPaperFilenameRpj(paper, file, user);
            permission = publicationService.getPaperPermissionFilenameRpj(paper, permissionFile, user, permission, thesisUploadPath);
        } else {
            redirectAttributes.addFlashAttribute("message", "Файл статьи  и/или разрешения на публикацию не выбран");
            return "redirect:/personal";
        }

        if (publicationService.paperExists(paper.getPaperName(), paper.getPaperId())) {
            redirectAttributes.addFlashAttribute("message", "Статья с таким названием уже существует");
            return "redirect:/personal";
        }
        publicationService.paperSaveRpj(paper, user, filenames.get("file"), permission);
        model.addAttribute("messageOk", "Статья успешно загружена. Ожидайте ответа рецензента на ваш email");
        return "redirect:/personal";
    }

    @GetMapping("/fullProgram")
    public String fullProgram(Principal principal,
                              Model model) {
        if (!userService.isAuthenticates(principal)) {
            List<Publication> publications = getAllPublicationsSort(model);
            List<PublicationDTO> publicationList = publicationService.getPublicationList(publications);
            model.addAttribute("publicationList", publicationList);
            Set<Section> sections = EnumSet.allOf(Section.class);
            model.addAttribute("sections", sections);
            return "fullProgram";
        }
        return "mainPage";
    }

    @GetMapping("/cities")
    public String cities(Principal principal,
                         Model model) {
        if (!userService.isAuthenticates(principal)) {
            getAllPublicationsSort(model);
            getAffiliation(model);
            Set<PresentationType> types = EnumSet.allOf(PresentationType.class);
            model.addAttribute("types", types);
            return "cities";
        }
        return "mainPage";
    }

    @GetMapping("/affiliation")
    public String affiliation(@ModelAttribute Affiliation affiliation,
                              Model model) {
        List<Affiliation> affiliations = affiliationRepo.findAll();
        model.addAttribute("affiliations", affiliations);
        return "adminAffiliation";
    }


    @GetMapping("/affiliation/{affiliationId}")
    public String affiliationGet(@PathVariable Affiliation affiliationId,
                                 Model model) {
        getAffiliationsScript(model);
        model.addAttribute("affiliation", affiliationId);
        return "adminAffiliationRed";
    }

    @PostMapping("/affiliation/{affiliationId}")
    public String affiliationRed(@PathVariable Affiliation affiliationId,
                                 @RequestParam String organizationFull,
                                 @RequestParam String organizationShort,
                                 @RequestParam String city,
                                 @RequestParam String country,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        model.addAttribute("affiliation", affiliationId);
        getAffiliationsScript(model);
        if (affiliationRepo.findByOrganizationShort(organizationShort) == null || affiliationRepo.findByOrganizationShort(organizationShort).getAffiliationId().equals(affiliationId.getAffiliationId())) {
            userService.affiliationSave(affiliationId, organizationFull, organizationShort, city, country);
            redirectAttributes.addFlashAttribute("messageOk", "Изменения сохранены");
        } else {
            redirectAttributes.addFlashAttribute("message", "Такая организация уже есть. Названия должны быть уникальными");
        }
        return "redirect:{affiliationId}";
    }

    @PostMapping("/affiliation/{affiliationId}/affiliationDelete")
    public String affiliationDelete(@PathVariable Affiliation affiliationId,
                                    RedirectAttributes redirectAttributes) {
        if (userRepo.findByAffiliationAffiliationId(affiliationId.getAffiliationId()) != null) {
            redirectAttributes.addFlashAttribute("message", userRepo.findByAffiliationAffiliationId(affiliationId.getAffiliationId()).getLastName()
                    + " " + userRepo.findByAffiliationAffiliationId(affiliationId.getAffiliationId()).getFirstName() + " использует данную аффилиацию ");
            return "redirect:/admin/affiliation/{affiliationId}";
        }
        if (!publicationCoauthorsRepo.findByCoauthorAffiliation_Affiliation_AffiliationId(affiliationId.getAffiliationId()).isEmpty()) {
            redirectAttributes.addFlashAttribute("message", "Аффилиация используется");
            return "redirect:/admin/affiliation/{affiliationId}";
        }
        publicationService.affiliationDelete(affiliationId.getAffiliationId());
        redirectAttributes.addFlashAttribute("messageOk", "Аффилиация удалена");
        return "redirect:/admin/affiliation";
    }

    @PostMapping(path = "/publicationDelete")
    public String publicationDelete(@RequestParam("publicationId") Long id,
                                    RedirectAttributes redirectAttributes) {
        publicationService.publicationDelete(id);
        redirectAttributes.addFlashAttribute("messageOk", "Тезисы успешно удалены");
        return "redirect:/personal";
    }

    @GetMapping("/statistics")
    public String statistics(Principal principal,
                             Model model) {
        if (!userService.isAuthenticates(principal)) {
            List<User> userList = (List<User>) userService.findAll();
            userList.removeIf(user -> !user.isAccountNonLocked() || !user.isEnabled());
            List<User> userListCurrent = new ArrayList<>();
            Set<String> citiesCurrent = new HashSet<>();
            int youngCurrent = 0;
            for (User user : userList) {
                if (user.getActualYear() == actualYear) {
                    userListCurrent.add(user);
                }
                if (user.getActualYear() == actualYear && user.isYoung()) {
                    youngCurrent++;
                }
                if (user.getActualYear() == actualYear && user.getAffiliation() != null) {
                    citiesCurrent.add(user.getAffiliation().getCity());
                }
            }
            List<User> adminList = new ArrayList<>();
            List<User> secretaryList = new ArrayList<>();
            List<User> editorList = new ArrayList<>();
            List<User> financierList = new ArrayList<>();
            int young = 0;
            for (User user : userList) {
                if (user.getRoles().contains(Role.ADMIN)) {
                    adminList.add(user);
                } else if (user.getRoles().contains(Role.SECRETARY)) {
                    secretaryList.add(user);
                } else if (user.getRoles().contains(Role.EDITOR)) {
                    editorList.add(user);
                } else if (user.getRoles().contains(Role.FINANCIER)) {
                    financierList.add(user);
                }
                if (user.isYoung()) {
                    young++;
                }
            }
            List<Affiliation> affiliationList = affiliationRepo.findAll();
            Set<String> cities = new HashSet<>();
            for (Affiliation affiliation : affiliationList) {
                cities.add(affiliation.getCity());
            }
            List<Publication> publications = (List<Publication>) publicationRepo.findAll();
            List<Publication> plenaryList = new ArrayList<>();
            List<Publication> invitedList = new ArrayList<>();
            List<Publication> sectionList = new ArrayList<>();
            List<Publication> posterList = new ArrayList<>();
            for (Publication publication : publications) {
                if (publication.getPresentationType().equals("Пленарный")) {
                    plenaryList.add(publication);
                }
                if (publication.getPresentationType().equals("Приглашенный")) {
                    invitedList.add(publication);
                }
                if (publication.getPresentationType().equals("Секционный")) {
                    sectionList.add(publication);
                }
                if (publication.getPresentationType().equals("Постерный")) {
                    posterList.add(publication);
                }
            }
            int publicationsAccept = 0;
            int publicationsWithFile = 0;
            for (Publication publication : publications) {
                if (publication.isAcceptThesis()) {
                    publicationsAccept++;
                }
            }
            for (Publication publication : publications) {
                if (publication.getFilename() != null) {
                    publicationsWithFile++;
                }
            }
            List<Paper> papers = getAllPapersSort(model);
            getAllPublicationsSort(model);
            Set<PresentationType> types = EnumSet.allOf(PresentationType.class);
            model.addAttribute("types", types);

            int papersAccept = 0;
            for (Paper paper : papers) {
                if (paper.getState().equals(PublicationState.ACCEPTED)) {
                    papersAccept++;
                }
            }
            model.addAttribute("usersCount", userList.size());
            model.addAttribute("usersCountCurrent", userListCurrent.size());
            model.addAttribute("young", young);
            model.addAttribute("youngCurrent", youngCurrent);
            model.addAttribute("cityCount", cities.size());
            model.addAttribute("cityCountCurrent", citiesCurrent.size());
            model.addAttribute("publicationCount", publications.size());
            model.addAttribute("publicationsAccept", publicationsAccept);
            model.addAttribute("publicationsWithFile", publicationsWithFile);
            model.addAttribute("plenaryCount", plenaryList.size());
            model.addAttribute("invitedCount", invitedList.size());
            model.addAttribute("sectionCount", sectionList.size());
            model.addAttribute("posterCount", posterList.size());
            model.addAttribute("paperCount", papers.size());
            model.addAttribute("papersAccept", papersAccept);
            model.addAttribute("adminList", adminList);
            model.addAttribute("secretaryList", secretaryList);
            model.addAttribute("editorList", editorList);
            model.addAttribute("financierList", financierList);

            return "statistics";
        }
        return "mainPage";
    }

    @GetMapping("/adminReviewThesis")
    public String adminReviewThesis(Principal principal,
                                    Model model) {
        if (!userService.isAuthenticates(principal)) {
            getAllPublicationsSort(model);
            return "adminReviewThesis";
        }
        return "mainPage";
    }

    @GetMapping("/usersContracts")
    public String adminUsersContracts(Principal principal,
                                      Model model) {
        if (!userService.isAuthenticates(principal)) {
            getAllUsersContractsSort(model);
            return "adminUsersContracts";
        }
        return "mainPage";
    }

    private void getAllUsersContractsSort(Model model) {
        List<UserContract> usersContracts = (List<UserContract>) userContractRepo.findAll();
        usersContracts.sort(Comparator.comparing(UserContract::getId));
        model.addAttribute("allUsersContracts", usersContracts);
    }

    @PostMapping("/usersContractsAccept")
    public String adminUsersContractsAccept(@ModelAttribute User user,
                                            @RequestParam("conrtactId") Long id,
                                            @RequestParam MultipartFile file,
                                            RedirectAttributes redirectAttributes) throws IOException {
        if (file != null && !Objects.requireNonNull(file.getOriginalFilename()).isEmpty()) {
            userService.setContractInvoiceFilename(id, file);
            UserContract userContract = userContractRepo.findById(id).get();
            mailSenderService.sendMessageContractInvoice(userContract.getUser());
        } else {
            redirectAttributes.addFlashAttribute("message", "Не выбран файл счета");
        }
        return "redirect:/admin/usersContracts";
    }

    @GetMapping("/userInvoiceGet")
    @ResponseBody
    public FileSystemResource downloadUserInvoice(@Param(value = "id") Long id) {
        UserContract userContract = userService.findByContractId(id);
        String filename = userContract.getInvoiceFilename();
        String fileURL = contractsUploadPath + "/" + filename;
        return new FileSystemResource(new File(fileURL));
    }

    @PostMapping(path = "/userInvoiceDelete")
    public String userInvoiceDelete(@ModelAttribute User user,
                                    @RequestParam("id") Long id,
                                    RedirectAttributes redirectAttributes) {
        UserContract userContract = userService.findByContractId(id);
        String userInvoiceFilename = userContract.getInvoiceFilename();
        File oldPaper = new File(contractsUploadPath + "/" + userInvoiceFilename);
        oldPaper.delete();
        userContract.setInvoiceFilename(null);
        redirectAttributes.addFlashAttribute("messageOk", "Счет успешно удален");
        return "redirect:/admin/usersContracts";
    }

    @PostMapping("/adminReviewThesisAccept")
    public String adminReviewThesisAccept(@ModelAttribute Publication publication,
                                          Model model) {
        getAllPublicationsSort(model);
        model.addAttribute("publicationId", publication.getPublicationId());
        Publication currentPublication = publicationRepo.findByPublicationId(publication.getPublicationId());
        currentPublication.setAcceptThesis(true);
        mailSenderService.sendMessageThesis(currentPublication.getUser(), currentPublication);
        return "adminReviewThesis";
    }

    @PostMapping("/adminReviewThesisAcceptPermission")
    public String adminReviewThesisAcceptPermission(@ModelAttribute Publication publication,
                                                    Model model) {
        getAllPublicationsSort(model);
        model.addAttribute("publicationId", publication.getPublicationId());
        Publication currentPublication = publicationRepo.findByPublicationId(publication.getPublicationId());
        currentPublication.setAcceptThesis(true);
        mailSenderService.sendMessageThesisPermission(currentPublication.getUser(), currentPublication);
        return "adminReviewThesis";
    }

    @GetMapping("/adminReviewThesis/{publicationId}")
    public String adminReviewThesisLetter(Principal principal,
                                          @PathVariable Publication publicationId,
                                          Model model) {
        if (!userService.isAuthenticates(principal)) {
            model.addAttribute("publication", publicationId);
            return "adminReviewThesisLetter";
        }
        return "mainPage";
    }

    @PostMapping("/adminReviewThesis/{publicationId}")
    public String adminReviewThesisLetter(@PathVariable Publication publicationId,
                                          @RequestParam("textField") String textField,
                                          Model model,
                                          RedirectAttributes redirectAttributes) {
        model.addAttribute("publication", publicationId);
        String publicationName = publicationId.getFilename();
        if (!textField.equals("")) {
            File oldPublication = new File(thesisUploadPath + "/" + publicationName);
            oldPublication.delete();
            publicationId.setFilename(null);
            publicationRepo.save(publicationId);
            mailSenderService.sendMessageThesisRed(publicationId.getUser(), publicationId, textField);
            redirectAttributes.addFlashAttribute("messageOk", "Сообщение отправлено");
            return "redirect:{publicationId}";
        } else {
            model.addAttribute("message", "Нельзя отправить пустое сообщение");
            return "adminReviewThesisLetter";
        }
    }

    @GetMapping("/adminReviewContract/{userId}/{contractId}")
    public String adminReviewContractLetter(Principal principal,
                                            @PathVariable User userId,
                                            @PathVariable UserContract contractId,
                                            Model model) {
        if (!userService.isAuthenticates(principal)) {
            model.addAttribute("user", userId);
            model.addAttribute("contractId", contractId);
            return "adminReviewContractLetter";
        }
        return "mainPage";
    }

    @GetMapping("/adminReviewContractExtra/{userId}")
    public String adminReviewContractExtraLetter(Principal principal,
                                                 @PathVariable User userId,
                                                 Model model) {
        if (!userService.isAuthenticates(principal)) {
            model.addAttribute("user", userId);
            return "adminReviewContractExtraLetter";
        }
        return "mainPage";
    }

    @GetMapping("/usersById")
    public String adminUsersById(Principal principal,
                                 Model model) {
        if (!userService.isAuthenticates(principal)) {
            List<User> userList = (List<User>) userService.findAll();
            model.addAttribute("userList", userList);
            return "adminUsersById";
        }
        return "mainPage";
    }

    @PostMapping("/adminReviewContract/{userId}/{contractId}")
    public String adminReviewContractLetter(@PathVariable User userId,
                                            @RequestParam("textField") String textField,
                                            @PathVariable Long contractId,
                                            RedirectAttributes redirectAttributes) {
        UserContract userContract = userService.findByContractId(contractId);
        String contractFilename = userContract.getContractFilename();
        if (!textField.equals("")) {
            File oldPublication = new File(contractsUploadPath + "/" + contractFilename);
            oldPublication.delete();
            userService.delete(userContract);
            mailSenderService.sendMessageContract(userId, textField);
            redirectAttributes.addFlashAttribute("messageOk", "Сообщение отправлено");
            return "redirect:/admin/usersContracts";
        } else {
            redirectAttributes.addFlashAttribute("message", "Нельзя отправить пустое сообщение");
            return "redirect:{contractId}";
        }
    }


    @GetMapping("/adminReviewPaper")
    public String adminReviewPaper(Principal principal,
                                   @ModelAttribute("paper") Paper paper,
                                   @ModelAttribute("state") String state,
                                   Model model) {
        if (!userService.isAuthenticates(principal)) {
            getAllPapersSort(model);
            return "adminReviewPaper";
        }
        return "mainPage";
    }

    @PostMapping("/adminReviewPaperChecked")
    public String adminReviewPaperChecked(Principal principal,
                                          @ModelAttribute("paper") Paper paper,
                                          @ModelAttribute("state") String state,
                                          @RequestParam MultipartFile file,
                                          @RequestParam MultipartFile paperFile,
                                          Model model,
                                          RedirectAttributes redirectAttributes) throws IOException {
        if (!userService.isAuthenticates(principal)) {
            getAllPapersSort(model);
            Paper currentPaper = paperRepo.findByPaperId(paper.getPaperId());
            String paperNameCut = nameCut(currentPaper);
            if (file != null && paperFile != null &&
                    !Objects.requireNonNull(file.getOriginalFilename()).isEmpty() &&
                    !Objects.requireNonNull(paperFile.getOriginalFilename()).isEmpty()) {
                String orig = file.getOriginalFilename();
                String paperOrig = paperFile.getOriginalFilename();
                File oldReview = new File(thesisUploadPath + "/" + currentPaper.getPaperReview());
                oldReview.delete();
                File oldPublication = new File(thesisUploadPath + "/" + currentPaper.getPaperFilename());
                oldPublication.delete();
                if (orig.contains(".")) {
                    String fileType = orig.substring(orig.lastIndexOf('.'));
                    String paperFileType = paperOrig.substring(paperOrig.lastIndexOf('.'));
                    String paperFilename = "";
                    if (currentPaper.getPaperFilename().contains("paper")) {
                        paperFilename = currentPaper.getUser().getLastName() + "_" + paperNameCut + "_paper" + paperFileType;
                        paperFile.transferTo(new File(thesisUploadPath + "/" + paperFilename));
                    }
                    if (currentPaper.getPaperFilename().contains("RPJ")) {
                        paperFilename = currentPaper.getUser().getLastName() + "_" + paperNameCut + "_RPJ" + paperFileType;
                        paperFile.transferTo(new File(thesisUploadPath + "/" + paperFilename));
                    }
                    String filename = currentPaper.getUser().getLastName() + "_" + paperNameCut + "_paper_review" + fileType;
                    file.transferTo(new File(thesisUploadPath + "/" + filename));
                    if (state.equals("CHECKED")) {
                        currentPaper.setState(PublicationState.CHECKED);
                        currentPaper.setPaperReview(filename);
                        currentPaper.setPaperFilename(paperFilename);
                        paperRepo.save(currentPaper);
                        mailSenderService.sendMessagePaperChecked(currentPaper.getUser(), currentPaper);
                        redirectAttributes.addFlashAttribute("messageOk", "Рецензия успешно загружена и отправлена автору");
                        return "redirect:/admin/adminReviewPaper";
                    } else if (state.equals("ACCEPTED")) {
                        currentPaper.setState(PublicationState.ACCEPTED);
                        currentPaper.setPaperReview(filename);
                        currentPaper.setPaperFilename(paperFilename);
                        paperRepo.save(currentPaper);
                        mailSenderService.sendMessagePaperAccepted(currentPaper.getUser(), currentPaper);
                        redirectAttributes.addFlashAttribute("messageOk", "Публикация принята, оповещение отправлено автору");
                        return "redirect:/admin/adminReviewPaper";
                    } else {
                        redirectAttributes.addFlashAttribute("message", "Действие не выбрано");
                        return "redirect:/admin/adminReviewPaper";
                    }
                }
            } else if (file != null && !Objects.requireNonNull(file.getOriginalFilename()).isEmpty()) {
                String orig = file.getOriginalFilename();
                File oldReview = new File(thesisUploadPath + "/" + currentPaper.getPaperReview());
                oldReview.delete();
                if (orig.contains(".")) {
                    String fileType = orig.substring(orig.lastIndexOf('.'));
                    String filename = currentPaper.getUser().getLastName() + "_" + paperNameCut + "_paper_review" + fileType;
                    file.transferTo(new File(thesisUploadPath + "/" + filename));
                    if (state.equals("CHECKED")) {
                        currentPaper.setState(PublicationState.CHECKED);
                        currentPaper.setPaperReview(filename);
                        paperRepo.save(currentPaper);
                        mailSenderService.sendMessagePaperChecked(currentPaper.getUser(), currentPaper);
                        redirectAttributes.addFlashAttribute("messageOk", "Рецензия успешно загружена и отправлена автору");
                        return "redirect:/admin/adminReviewPaper";
                    } else if (state.equals("ACCEPTED")) {
                        currentPaper.setState(PublicationState.ACCEPTED);
                        currentPaper.setPaperReview(filename);
                        paperRepo.save(currentPaper);
                        mailSenderService.sendMessagePaperAccepted(currentPaper.getUser(), currentPaper);
                        redirectAttributes.addFlashAttribute("messageOk", "Публикация принята, оповещение отправлено автору");
                        return "redirect:/admin/adminReviewPaper";
                    } else {
                        redirectAttributes.addFlashAttribute("message", "Действие не выбрано");
                        return "redirect:/admin/adminReviewPaper";
                    }
                }
            } else {
                redirectAttributes.addFlashAttribute("message", "Файл рецензии не выбран");
            }
            return "redirect:/admin/adminReviewPaper";
        }
        return "mainPage";
    }

    public static String nameCut(Paper currentPaper) {
        String paperNameCut;
        if (currentPaper.getPaperName().length() > 50) {
            paperNameCut = currentPaper.getPaperName().substring(0, 49);
        } else {
            paperNameCut = currentPaper.getPaperName();
        }
        if (paperNameCut.contains("/")) {
            paperNameCut = paperNameCut.replace("/", "_");
        }
        return paperNameCut;
    }

    @GetMapping("/addUser")
    public String addUser(Principal principal,
                          @ModelAttribute("userRoleName") String userRoleName,
                          @ModelAttribute("affiliation") Affiliation affiliation,
                          @ModelAttribute("user") User user,
                          Model model) {
        if (!userService.isAuthenticates(principal)) {
            getAllPublicationsSort(model);
            getAffiliation(model);
            Set<PresentationType> types = EnumSet.allOf(PresentationType.class);
            model.addAttribute("types", types);
            return "adminAddUser";
        }
        return "mainPage";
    }

    @PostMapping(path = "/addUser")
    public String addUser(Principal principal,
                          @RequestParam("confirmation") String confirmation,
                          @ModelAttribute("userRoleName") String userRoleName,
                          @ModelAttribute("user") @Valid User user,
                          BindingResult result,
                          Model model) {

        model.addAttribute("authentic", userService.isAuthenticates(principal));
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
            return "adminAddUser";
        }

        if (!userService.addUserAdmin(user, userRoleName) || !passwordEncoder.matches(confirmation, user.getPassword())) {
            if (!passwordEncoder.matches(confirmation, user.getPassword())) {
                model.addAttribute("mess", "Пароли не совпадают");
            }
            Map<String, String> errors = ControllerUtils.getErrors(result);
            System.out.println(errors);
            model.mergeAttributes(errors);
            model.addAttribute("message", "Ошибка регистрации");
            if (!userService.addUserAdmin(user, userRoleName)) {
                model.addAttribute("message", "Данный пользователь существует!");
            }
            return "adminAddUser";
        }

        model.addAttribute("messageOk", "Пользователь зарегистрирован. " +
                "Для добавления доклада перейдите в список Участники без доклада");
        return "adminAddUser";
    }

    private List<Publication> getAllPublicationsSort(Model model) {
        List<Publication> publications = (List<Publication>) publicationRepo.findAll();
        model.addAttribute("allPublications", publications);
        publications.sort(Comparator.comparing(o -> o.getUser().getLastName()));
        model.addAttribute("allPublicationsSort", publications);
        return publications;
    }

    private void getAffiliation(Model model) {
        List<User> userList = (List<User>) userService.findAll();
        List<User> userListCurrent = userList;
        userList.removeIf(user -> !user.isActive());
        userListCurrent.removeIf(user -> user.getActualYear() != actualYear || !user.isAccountNonLocked() || !user.isEnabled());
        Set<String> citiesCurrent = new TreeSet<>();
        for (User user : userListCurrent) {
            if (user.getAffiliation() != null)
                citiesCurrent.add(user.getAffiliation().getCity());
        }
        model.addAttribute("allUsers", userList);
        model.addAttribute("allUsersCurrent", userListCurrent);
        model.addAttribute("usersCount", userList.size());
        model.addAttribute("usersCountCurrent", userListCurrent.size());
        model.addAttribute("citiesCurrent", citiesCurrent);
        model.addAttribute("cityCountCurrent", citiesCurrent.size());
    }

    private List<Paper> getAllPapersSort(Model model) {
        List<Paper> papers = (List<Paper>) paperRepo.findAll();
        model.addAttribute("allPapers", papers);
//        papers.sort(Comparator.comparing(o -> o.getUser().getId()));
        model.addAttribute("allPapersSort", papers);
        return papers;
    }

    private void getAffiliationsScript(Model model) {
        Set<Affiliation> affiliationList = new HashSet<>();
        Set<String> allOrganizationFull = new HashSet<>();
        Set<String> allOrganizationShort = new HashSet<>();
        Set<String> allCities = new HashSet<>();
        Set<String> allCountries = new HashSet<>();
        affiliationRepo.findAll().forEach(affiliationList::add);
        for (Affiliation affil : affiliationList) {
            allOrganizationFull.add(affil.getOrganizationFull());
            allOrganizationShort.add(affil.getOrganizationShort());
            allCities.add(affil.getCity());
            allCountries.add(affil.getCountry());
        }
        model.addAttribute("affiliationList", affiliationList);
        model.addAttribute("allOrganizationFull", allOrganizationFull);
        model.addAttribute("allOrganizationShort", allOrganizationShort);
        model.addAttribute("allCities", allCities);
        model.addAttribute("allCountries", allCountries);
    }
}
