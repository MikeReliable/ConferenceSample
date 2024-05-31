package com.mike.service;

import com.mike.domain.*;
import com.mike.repos.AffiliationRepo;
import com.mike.repos.CoauthorAffiliationRepo;
import com.mike.repos.UserContractRepo;
import com.mike.repos.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.security.Principal;
import java.time.Instant;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private MailSenderService mailSenderService;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private AffiliationRepo affiliationRepo;

    @Autowired
    private UserContractRepo userContractRepo;

    @Autowired
    private CoauthorAffiliationRepo coauthorAffiliationRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${contracts.upload.path}")
    private String contractsUploadPath;

    @Value("${actual.year}")
    private int actualYear;

    @Override
    public User loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepo.findByEmail(email);
    }

    public UserDetails loadUserByActivationCode(String code) throws UsernameNotFoundException {
        return userRepo.findByActivationCode(code);
    }

    public Iterable<User> findAll() {
        return userRepo.findAll();
    }

    public Iterable<CoauthorAffiliation> findAllCoauthors() {
        return coauthorAffiliationRepo.findAll();
    }

    public Iterable<User> findAllWithThesis() {
        Iterable<User> users = userRepo.findAll();
        List<User> userList = new ArrayList<>();
        for (User user : users) {
            if (user.getAffiliation() != null && !user.getPublicationSet().isEmpty()) {
                userList.add(user);
            }
        }
        userList.sort(Comparator.comparing(User::getLastName));
        return userList;
    }

    public Set<UserDTO> getUserDTOArray() {
        Set<UserDTO> userDTOArray = new HashSet<>();
        Iterable<User> usersList = findAll();
        Iterable<CoauthorAffiliation> coauthorAffiliationList = findAllCoauthors();
        for (User user : usersList) {
            if (user.isAccountNonLocked()) {
                UserDTO userDTO = setUserDTO(user);
                userDTOArray.add(userDTO);
            }
        }
        for (CoauthorAffiliation coauthorAffiliation : coauthorAffiliationList) {
            UserDTO userDTO = new UserDTO();
            userDTO.setFirstName(coauthorAffiliation.getCoauthor().getFirstName());
            userDTO.setLastName(coauthorAffiliation.getCoauthor().getLastName());
            userDTO.setMiddleName(coauthorAffiliation.getCoauthor().getMiddleName());
            if (coauthorAffiliation.getAffiliation() != null) {
                userDTO.setOrganizationShort(coauthorAffiliation.getAffiliation().getOrganizationShort());
                userDTO.setCity(coauthorAffiliation.getAffiliation().getCity());
            }
            userDTOArray.add(userDTO);
        }
        return userDTOArray;
    }

    public UserDTO setUserDTO(User user) {
        UserDTO userDTO = new UserDTO();
        userDTO.setFirstName(user.getFirstName());
        userDTO.setLastName(user.getLastName());
        userDTO.setMiddleName(user.getMiddleName());
        if (user.getAffiliation() != null) {
            userDTO.setOrganizationShort(user.getAffiliation().getOrganizationShort());
            userDTO.setCity(user.getAffiliation().getCity());
        }
        return userDTO;
    }

    public boolean addUserEn(User user, Affiliation affiliation, Publication publication, String section) {

        String firstName = user.getFirstName();
        String lastName = user.getLastName();
        String email = user.getEmail();
        String degree = user.getDegree();
        String telephone = user.getTelephone();
        String organization = affiliation.getOrganizationShort();
        String city = affiliation.getCity();
        String country = affiliation.getCountry();
        boolean young = user.isYoung();
        String publ = publication.getPublicationName();

        String sect = null;

        switch (section) {
            case ("1"):
                sect = Section.SECTION_1.getSection();
                break;
            case ("2"):
                sect = Section.SECTION_2.getSection();
                break;
            case ("3"):
                sect = Section.SECTION_3.getSection();
                break;
            case ("4"):
                sect = Section.SECTION_4.getSection();
                break;
            default:
                return false;
        }
        mailSenderService.sendMessageEn(firstName, lastName, email, degree, telephone, organization, city, country, young, publ, sect);
        return true;
    }

    public void affiliationSave(Affiliation affiliation,
                                String organizationFull,
                                String organizationShort,
                                String city,
                                String country) {
        affiliation.setOrganizationFull(organizationFull);
        affiliation.setOrganizationShort(organizationShort);
        affiliation.setCity(city);
        affiliation.setCountry(country);
        affiliationRepo.save(affiliation);
    }

    public Map<String, String> affiliationSaveNew(BindingResult result,
                                                  String organizationFull,
                                                  String organizationShort,
                                                  String city,
                                                  String country) {
        Map<String, String> messages = new HashMap<>();
        Map<String, String> errors = ControllerUtils.getErrors(result);
        Affiliation affiliation = new Affiliation();
        if (!errors.isEmpty()) {
            messages.put("message", "Ошибка заполнения формы");
        }
        if (organizationFull.isEmpty() || organizationShort.isEmpty() || city.isEmpty() || country.isEmpty()) {
            messages.put("organizationFull", "Поле не может быть пустым");
            messages.put("organizationShort", "Поле не может быть пустым");
            messages.put("city", "Поле не может быть пустым");
            messages.put("country", "Поле не может быть пустым");
        } else {
            if (affiliationRepo.findByOrganizationShort(organizationShort) != null) {
                messages.put("message", "Данная ораганизация уже присутствует в базе");
            } else {
                affiliation.setOrganizationFull(organizationFull);
                affiliation.setOrganizationShort(organizationShort);
                affiliation.setCity(city);
                affiliation.setCountry(country);
                affiliationRepo.save(affiliation);
                messages.put("messageOk", "Новая аффилиация успешно добавлена");
            }
        }
        return messages;
    }

    public boolean addUser(User user) {
        User userFromDb = userRepo.findByEmail(user.getEmail());
        User userNameFromDb = userRepo.findByFirstNameAndMiddleNameAndLastName(user.getFirstName(), user.getMiddleName(), user.getLastName());
        Pattern blockNumber = Pattern.compile("\\d");
        Pattern blockSymbol = Pattern.compile("\\w");
        Pattern blockCapital = Pattern.compile("[A-Z]{2}");

        if (userFromDb != null || userNameFromDb != null) {
            return false;
        }

        user.setFirstName(user.getFirstName());
        user.setMiddleName(user.getMiddleName());
        user.setLastName(user.getLastName());
        user.setEmail(user.getEmail());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setDegree(user.getDegree());
        user.setPosition(user.getPosition());
        user.setYoung(user.isYoung());
        user.setTelephone(user.getTelephone());
        user.setActive(false);
        user.setActualYear(actualYear);        
        user.setRoles(Collections.singleton(Role.USER));
        user.setActivationCode(UUID.randomUUID().toString());
        Matcher matcherNumber = blockNumber.matcher(user.getLastName());
        Matcher matcherSymbol = blockSymbol.matcher(user.getLastName());
        Matcher matcherCapital = blockCapital.matcher(user.getLastName());
        if (matcherNumber.find() || matcherSymbol.find() || matcherCapital.find()) {
            user.setBlocked(true);
        } else {
            user.setBlocked(false);
            mailSenderService.sendMessage(user);
        }
        userRepo.save(user);
        return true;
    }

    public boolean addUserAdmin(User user, String userRoleName) {
        User userFromDb = userRepo.findByEmail(user.getEmail());
        if (userFromDb != null) {
            return false;
        }
        Set<Role> roleSet = new HashSet<>();
        if (!userRoleName.equals("0")) {
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
            }
        } else {
            roleSet.add(Role.USER);
        }
        user.setRoles(roleSet);
        user.setFirstName(user.getFirstName());
        user.setMiddleName(user.getMiddleName());
        user.setLastName(user.getLastName());
        user.setEmail(user.getEmail());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setDegree(user.getDegree());
        user.setPosition(user.getPosition());
        user.setYoung(user.isYoung());
        user.setTelephone(user.getTelephone());
        user.setActive(true);
        user.setActualYear(actualYear);
        userRepo.save(user);
        return true;
    }

    public boolean activateUser(String code) {
        User user = userRepo.findByActivationCode(code);
        if (user == null) {
            return false;
        }
        user.setActive(true);
        user.setActivationCode(null);
        userRepo.save(user);
        return true;
    }

    public boolean isAuthenticates(Principal principal) {
        return principal == null;
    }

    public void passwordChange(String password, String code) {
        User currentUser = (User) loadUserByActivationCode(code);
        currentUser.setPassword(passwordEncoder.encode(password));
        currentUser.setActive(true);
        currentUser.setActivationCode(null);
        userRepo.save(currentUser);
    }

    public void updatePersonal(User user, String firstName, String middleName, String lastName,
                               String degree, String position, String telephone, boolean young) {
        user.setFirstName(firstName);
        user.setMiddleName(middleName);
        user.setLastName(lastName);
        user.setEmail(user.getEmail());
        user.setDegree(degree);
        user.setPosition(position);
        user.setYoung(young);
        user.setTelephone(telephone);
        userRepo.save(user);
    }

    public void setContractFilename(MultipartFile file, User currentUser, String contractType) throws IOException {
        File uploadDir = new File(contractsUploadPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdir();
        }
        String orig = file.getOriginalFilename();
        String fileType = orig.substring(orig.lastIndexOf('.'));
        UserContract userContract = new UserContract();
        Instant instant = Instant.now();
        String instantStr = instant.toString().substring(0, 19).replace(':', '-');
        String filename = currentUser.getLastName() + "_" + currentUser.getFirstName() + "_contract_" + instantStr + fileType;
        userContract.setContractFilename(filename);
        userContract.setContractType(contractType);
        userContract.setUser(currentUser);
        userContractRepo.save(userContract);
        file.transferTo(new File(contractsUploadPath + "/" + filename));
    }

    public UserContract findByContractId(Long id) {
        return userContractRepo.findById(id).get();
    }

    public void setContractInvoiceFilename(Long id, MultipartFile file) throws IOException {
        UserContract userContract = findByContractId(id);
        String orig = file.getOriginalFilename();
        String fileType = orig.substring(orig.lastIndexOf('.'));
        String filename = userContract.getContractFilename().substring(0, userContract.getContractFilename().lastIndexOf('.')) + "_invoice" + fileType;
        userContract.setInvoiceFilename(filename);
        file.transferTo(new File(contractsUploadPath + "/" + filename));
    }

    public void setContractCheckFilename(MultipartFile file, User currentUser, Long id) throws IOException {
        UserContract userContract = findByContractId(id);
        String orig = file.getOriginalFilename();
        String fileType = orig.substring(orig.lastIndexOf('.'));
        String filename = userContract.getContractFilename().substring(0, userContract.getContractFilename().lastIndexOf('.')) + "_check" + fileType;
        userContract.setCheckFilename(filename);
        userContract.setUser(currentUser);
        userRepo.save(currentUser);
        file.transferTo(new File(contractsUploadPath + "/" + filename));
    }

    public void delete(UserContract userContract) {
//        userContract.getUser().setUserContract(null);
        userContractRepo.delete(userContract);
    }
}
