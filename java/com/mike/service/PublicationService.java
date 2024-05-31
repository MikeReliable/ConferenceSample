package com.mike.service;

import com.mike.controller.AdminController;
import com.mike.domain.*;
import com.mike.repos.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Service
public class PublicationService implements UserDetailsService {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private PublicationRepo publicationRepo;

    @Autowired
    private AffiliationRepo affiliationRepo;

    @Autowired
    private PaperRepo paperRepo;

    @Autowired
    private CoauthorRepo coauthorRepo;

    @Autowired
    private CoauthorAffiliationRepo coauthorAffiliationRepo;

    @Autowired
    private PublicationCoauthorsRepo publicationCoauthorsRepo;

    @Value("${upload.path}")
    private String thesisUploadPath;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepo.findByEmail(email);
    }

    public boolean thesisExists(String publicationName) {
        return publicationRepo.findByPublicationName(publicationName) != null;
    }

    public boolean paperExists(String paperName, Long paperId) {
        return paperRepo.findByPaperName(paperName) != paperRepo.findByPaperId(paperId);
    }

    public Publication findByPublicationId(Long id) {
        return publicationRepo.findByPublicationId(id);
    }

    public Paper findByPaperId(Long id) {
        return paperRepo.findByPaperId(id);
    }


    public Iterable<Publication> findAllPublications() {
        return publicationRepo.findAll();
    }

    public List<PublicationDTO> getPublicationList(List<Publication> publications) {
        List<PublicationDTO> publicationList = new ArrayList<>();
        for (Publication publ : publications) {
            List<PublicationCoauthorsDTO> publicationCoauthorsDTOList = new ArrayList<>();
            Set<String> affil = new LinkedHashSet<>();
            PublicationDTO publicationDTO = new PublicationDTO();
            publicationDTO.setPublicationId(publ.getPublicationId());
            publicationDTO.setPublicationName(publ.getPublicationName());
            publicationDTO.setPublicationType(publ.getPresentationType());
            publicationDTO.setPublicationSection(publ.getSectionName());
            publicationDTO.setAcceptThesis(publ.isAcceptThesis());
            publicationDTO.setPublicationFilename(publ.getFilename());
            publicationDTO.setPublicationPermission(publ.getPermission());
            for (PublicationCoauthor publicationCoauthor : publ.getPublicationCoauthors()) {
                PublicationCoauthorsDTO publicationCoauthorsDTO = new PublicationCoauthorsDTO();
                publicationCoauthorsDTO.setPlace(publicationCoauthor.getPlace());
                publicationCoauthorsDTO.setOrganizationShort(publicationCoauthor.getCoauthorAffiliation().getAffiliation().getOrganizationShort());
                publicationCoauthorsDTO.setCity(publicationCoauthor.getCoauthorAffiliation().getAffiliation().getCity());
                publicationCoauthorsDTO.setFirstName(publicationCoauthor.getCoauthorAffiliation().getCoauthor().getFirstName());
                publicationCoauthorsDTO.setMiddleName(publicationCoauthor.getCoauthorAffiliation().getCoauthor().getMiddleName());
                publicationCoauthorsDTO.setLastName(publicationCoauthor.getCoauthorAffiliation().getCoauthor().getLastName());
                publicationCoauthorsDTO.setSpeaker(publicationCoauthor.isSpeaker());
                publicationCoauthorsDTOList.add(publicationCoauthorsDTO);
            }
            publicationCoauthorsDTOList.sort(Comparator.comparing(PublicationCoauthorsDTO::getPlace));
            for (PublicationCoauthorsDTO publicationCoauthorsDTO : publicationCoauthorsDTOList) {
                affil.add(publicationCoauthorsDTO.getOrganizationShort());
            }
            int i = 1;
            boolean mediatorNum = true;
            for (String s : affil) {
                for (PublicationCoauthorsDTO list : publicationCoauthorsDTOList) {
                    if (list.getOrganizationShort().equals(s)) {
                        if (mediatorNum) {
                            list.setNum(i);
                            mediatorNum = false;
                        } else {
                            list.setNum(i);
                            list.setOrganizationShort("");
                            list.setCity("");
                        }
                        if (affil.size() == i) {
                            list.setMaxNum(true);
                            if (affil.size() == 1) {
                                list.setNum(0);
                            }
                        }
                    }
                }
                i++;
                mediatorNum = true;
            }
            for (PublicationCoauthorsDTO publicationCoauthorsDTO : publicationCoauthorsDTOList) {
                List<Integer> affiliationsNum = new ArrayList<>();
                boolean dbl = true;
                String coauthor = publicationCoauthorsDTO.getFirstName() + " " + publicationCoauthorsDTO.getMiddleName() + " " + publicationCoauthorsDTO.getLastName();
                for (PublicationCoauthorsDTO coauthorsDTO : publicationCoauthorsDTOList) {
                    if ((coauthorsDTO.getFirstName() + " " + coauthorsDTO.getMiddleName() + " " + coauthorsDTO.getLastName()).equals(coauthor)) {
                        if (coauthorsDTO.isDoubleCoautor() || !coauthorsDTO.isDoubleCoautor()) {
                            if (dbl) {
                                affiliationsNum.add(coauthorsDTO.getNum());
                                coauthorsDTO.setDoubleCoautor(false);
                                dbl = false;
                            } else {
                                coauthorsDTO.setDoubleCoautor(true);
                                affiliationsNum.add(coauthorsDTO.getNum());
                            }

                        }
                    }
                }
                publicationCoauthorsDTO.setNumList(affiliationsNum);
            }
            publicationDTO.setPublicationCoauthorsDTOList(publicationCoauthorsDTOList);
            publicationList.add(publicationDTO);
        }
        return publicationList;
    }


    public List<Publication> getAllUserPublicationsSort(User user) {
        return publicationRepo.findAllByUser_Id(user.getId());
    }

    public boolean publicationSectionAndType(String section, String type) {
        if (section.equals("ВЫБЕРИТЕ СЕКЦИЮ*")) {
            return false;
        }
        switch (type) {
            case ("Секционный"):
            case ("Постерный"):
                break;
            default:
                return false;
        }
        return true;
    }

    public String getPaperPermissionFilename(Paper paper, MultipartFile permissionFile, User currentUser, String permission, String thesisUploadPath) throws IOException {
        String origPerm = permissionFile.getOriginalFilename();
        String paperNameCut = AdminController.nameCut(paper);
        if (origPerm.contains(".")) {
            String fileType = origPerm.substring(origPerm.lastIndexOf('.'));
            permission = currentUser.getLastName() + "_" + paperNameCut + "_paper" + "_permission" + fileType;
            permissionFile.transferTo(new File(thesisUploadPath + "/" + permission));
        }
        return permission;
    }

    public String getPaperPermissionFilenameRpj(Paper paper, MultipartFile permissionFile, User currentUser, String permission, String thesisUploadPath) throws IOException {
        String origPerm = permissionFile.getOriginalFilename();
        String paperNameCut = AdminController.nameCut(paper);
        if (origPerm.contains(".")) {
            String fileType = origPerm.substring(origPerm.lastIndexOf('.'));
            permission = currentUser.getLastName() + "_" + paperNameCut + "_RPJ" + "_permission" + fileType;
            permissionFile.transferTo(new File(thesisUploadPath + "/" + permission));
        }
        return permission;
    }

    public Map<String, String> getPaperFilename(Paper paper, MultipartFile file, MultipartFile filePdf, User currentUser) throws IOException {
        String orig = file.getOriginalFilename();
        String paperNameCut;
        String filenamePdf;
        Map<String, String> filenames = new HashMap<>();
        if (paper.getPaperName().length() > 50) {
            paperNameCut = paper.getPaperName().substring(0, 49);
        } else {
            paperNameCut = paper.getPaperName();
        }
        if (paperNameCut.contains("/")) {
            paperNameCut = paperNameCut.replace("/", "_");
        }
        if (orig.contains(".")) {
            String fileType = orig.substring(orig.lastIndexOf('.'));
            if (fileType.equals(".doc") || fileType.equals(".docx") || fileType.equals(".rtf")) {
                String filename = currentUser.getLastName() + "_" + paperNameCut + "_paper" + fileType;
                filenamePdf = currentUser.getLastName() + "_" + paperNameCut + "_paper_AIP.pdf";
                file.transferTo(new File(thesisUploadPath + "/" + filename));
                filePdf.transferTo(new File(thesisUploadPath + "/" + filenamePdf));
                filenames.put("file", filename);
                filenames.put("filePdf", filenamePdf);
            } else {
                filenames.put("message", "Выбран несоответствующий тип файла статьи");
            }
        }
        return filenames;
    }

    public Map<String, String> getPaperFilenameRpj(Paper paper, MultipartFile file, User currentUser) throws IOException {
        String orig = file.getOriginalFilename();
        String paperNameCut;
        Map<String, String> filenames = new HashMap<>();
        if (paper.getPaperName().length() > 50) {
            paperNameCut = paper.getPaperName().substring(0, 49);
        } else {
            paperNameCut = paper.getPaperName();
        }
        if (paperNameCut.contains("/")) {
            paperNameCut = paperNameCut.replace("/", "_");
        }
        if (orig.contains(".")) {
            String fileType = orig.substring(orig.lastIndexOf('.'));
            if (fileType.equals(".doc") || fileType.equals(".docx") || fileType.equals(".rtf")) {
                String filename = currentUser.getLastName() + "_" + paperNameCut + "_RPJ" + fileType;
                file.transferTo(new File(thesisUploadPath + "/" + filename));
                filenames.put("file", filename);
            } else {
                filenames.put("message", "Выбран несоответствующий тип файла статьи");
            }
        }
        return filenames;
    }

    public void publicationDelete(Long id) {
        Publication currentPublication = findByPublicationId(id);
        String publicationName = currentPublication.getFilename();
        String permissionName = currentPublication.getPermission();
        String posterName = currentPublication.getPoster();
        publicationCoauthorsRepo.deleteAllByPublication(currentPublication);
        File oldPublication = new File(thesisUploadPath + "/" + publicationName);
        File oldPermission = new File(thesisUploadPath + "/" + permissionName);
        File oldPoster = new File(thesisUploadPath + "/" + posterName);
        oldPublication.delete();
        oldPermission.delete();
        oldPoster.delete();
        publicationRepo.deleteByPublicationId(id);
    }

    public void paperDelete(Long paperId) {
        paperRepo.deleteByPaperId(paperId);
    }

    public void publicationPosterLoad(Long id, String posterName) {
        Publication publication = publicationRepo.findByPublicationId(id);
        publication.setPoster(posterName);
        publicationRepo.save(publication);
    }

    public void paperSave(Paper paper, User user, String filename, String filenamePdf, String permission) {
        paper.setPaperName(paper.getPaperName());
        paper.setPaperFilename(filename);
        paper.setPaperFilenamePdf(filenamePdf);
        paper.setPaperPermission(permission);
        paper.setUser(user);
        paper.setState(PublicationState.NEW);
        paperRepo.save(paper);
    }

    public void paperSaveRpj(Paper paper, User user, String filename, String permission) {
        paper.setPaperName(paper.getPaperName());
        paper.setPaperFilename(filename);
        paper.setPaperPermission(permission);
        paper.setUser(user);
        paper.setState(PublicationState.NEW);
        paperRepo.save(paper);
    }

    public void paperUpdate(Long paperId, String paperName, String filename, String filenamePdf, String permission) {
        Paper paper = paperRepo.findByPaperId(paperId);
        paper.setPaperName(paperName);
        paper.setPaperFilename(filename);
        paper.setPaperFilenamePdf(filenamePdf);
        paper.setPaperPermission(permission);
        paper.setState(PublicationState.NEW);
        paperRepo.save(paper);
    }

    public boolean publicationSave(String publicationName, String section, String type,
                                   User currentUser, Publication publication, String fileName, String permissionName,
                                   String firstName0, String middleName0, String lastName0, String organizationShort0,
                                   String firstName1, String middleName1, String lastName1, String organizationShort1,
                                   String firstName2, String middleName2, String lastName2, String organizationShort2,
                                   String firstName3, String middleName3, String lastName3, String organizationShort3,
                                   String firstName4, String middleName4, String lastName4, String organizationShort4,
                                   String firstName5, String middleName5, String lastName5, String organizationShort5,
                                   String firstName6, String middleName6, String lastName6, String organizationShort6,
                                   String firstName7, String middleName7, String lastName7, String organizationShort7,
                                   String firstName8, String middleName8, String lastName8, String organizationShort8,
                                   String firstName9, String middleName9, String lastName9, String organizationShort9) {
        String firstTest = null;
        String middleTest = null;
        String organizationTest = null;
        String lastTest = null;
        String first = null;
        String middle = null;
        String last = null;
        String organization = null;
        for (int j = 0; j <= 9; j++) {
            if (j == 0) {
                firstTest = firstName0;
                middleTest = middleName0;
                lastTest = lastName0;
                organizationTest = organizationShort0;
            }
            if (j == 1) {
                firstTest = firstName1;
                middleTest = middleName1;
                lastTest = lastName1;
                organizationTest = organizationShort1;
            }
            if (j == 2) {
                firstTest = firstName2;
                middleTest = middleName2;
                lastTest = lastName2;
                organizationTest = organizationShort2;
            }
            if (j == 3) {
                firstTest = firstName3;
                middleTest = middleName3;
                lastTest = lastName3;
                organizationTest = organizationShort3;
            }
            if (j == 4) {
                firstTest = firstName4;
                middleTest = middleName4;
                lastTest = lastName4;
                organizationTest = organizationShort4;
            }
            if (j == 5) {
                firstTest = firstName5;
                middleTest = middleName5;
                lastTest = lastName5;
                organizationTest = organizationShort5;
            }
            if (j == 6) {
                firstTest = firstName6;
                middleTest = middleName6;
                lastTest = lastName6;
                organizationTest = organizationShort6;
            }
            if (j == 7) {
                first = firstName7;
                middleTest = middleName7;
                lastTest = lastName7;
                organizationTest = organizationShort7;
            }
            if (j == 8) {
                firstTest = firstName8;
                middleTest = middleName8;
                lastTest = lastName8;
                organizationTest = organizationShort8;
            }
            if (j == 9) {
                firstTest = firstName9;
                middleTest = middleName9;
                lastTest = lastName9;
                organizationTest = organizationShort9;
            }
            User usr = userRepo.findByFirstNameAndMiddleNameAndLastName(firstTest, middleTest, lastTest);
            if (usr != null) {
                if (usr.equals(currentUser)) {
                    if (!organizationTest.equals("Организация")) {
                        if (publication.getPublicationCoauthors() != null) {
                            publicationCoauthorsRepo.deleteAllByPublication(publication);
                        }
                        publication.setFilename(fileName);
                        publication.setPermission(permissionName);
                        publication.setPublicationName(publicationName);
                        publication.setPresentationType(type);
                        publication.setSectionName(section);
                        publicationRepo.save(publication);
                        boolean rewrite = true;
                        for (int i = 0; i <= 9; i++) {
                            if (i == 0) {
                                first = firstName0;
                                middle = middleName0;
                                last = lastName0;
                                organization = organizationShort0;
                            }
                            if (i == 1) {
                                first = firstName1;
                                middle = middleName1;
                                last = lastName1;
                                organization = organizationShort1;
                            }
                            if (i == 2) {
                                first = firstName2;
                                middle = middleName2;
                                last = lastName2;
                                organization = organizationShort2;
                            }
                            if (i == 3) {
                                first = firstName3;
                                middle = middleName3;
                                last = lastName3;
                                organization = organizationShort3;
                            }
                            if (i == 4) {
                                first = firstName4;
                                middle = middleName4;
                                last = lastName4;
                                organization = organizationShort4;
                            }
                            if (i == 5) {
                                first = firstName5;
                                middle = middleName5;
                                last = lastName5;
                                organization = organizationShort5;
                            }
                            if (i == 6) {
                                first = firstName6;
                                middle = middleName6;
                                last = lastName6;
                                organization = organizationShort6;
                            }
                            if (i == 7) {
                                first = firstName7;
                                middle = middleName7;
                                last = lastName7;
                                organization = organizationShort7;
                            }
                            if (i == 8) {
                                first = firstName8;
                                middle = middleName8;
                                last = lastName8;
                                organization = organizationShort8;
                            }
                            if (i == 9) {
                                first = firstName9;
                                middle = middleName9;
                                last = lastName9;
                                organization = organizationShort9;
                            }
                            if (first == null && middle == null && last == null && organization == null) {
                                first = "";
                                middle = "";
                                last = "";
                                organization = "Организация";
                            }
                            if (!Objects.equals(first, "") && !Objects.equals(last, "") && !Objects.equals(organization, "Организация")) {
                                PublicationCoauthor publicationCoauthor = new PublicationCoauthor();

                                Coauthor coauthor = coauthorRepo.findByFirstNameAndMiddleNameAndLastName(first, middle, last);
                                Affiliation affiliation = affiliationRepo.findByOrganizationShort(organization);
                                CoauthorAffiliation coauthorAffiliation = coauthorAffiliationRepo.findByCoauthorAndAffiliation(coauthor, affiliation);
                                if (coauthorAffiliation == null) {
                                    CoauthorAffiliation coAffil = new CoauthorAffiliation();
                                    if (coauthor == null) {
                                        coauthor = new Coauthor(first, middle, last);
                                        coauthorRepo.save(coauthor);
                                    }
                                    coAffil.setCoauthor(coauthor);
                                    coAffil.setAffiliation(affiliation);
                                    coauthorAffiliationRepo.save(coAffil);
                                    coauthorAffiliation = coAffil;
                                }
                                User user = userRepo.findByFirstNameAndMiddleNameAndLastName(first, middle, last);
                                if (user != null) {
                                    if (user.equals(currentUser)) {
                                        if (user.getAffiliation() == null) {
                                            user.setAffiliation(affiliationRepo.findByOrganizationShort(organization));
                                            rewrite = false;
                                        } else {
                                            if (!user.getAffiliation().getOrganizationShort().equals(organization) && rewrite) {
                                                user.setAffiliation(affiliationRepo.findByOrganizationShort(organization));
                                                rewrite = false;
                                            }
                                        }
                                        userRepo.save(user);
                                        publication.setUser(user);
                                        publicationRepo.save(publication);
                                    }
                                }
                                if (publicationCoauthorsRepo.findByPublication(publication).isEmpty()) {
                                    publicationCoauthor.setCoauthorAffiliation(coauthorAffiliation);
                                    publicationCoauthor.setPlace(i);
                                    if (publication.getUser() != null && currentUser.equals(user)) {
                                        publicationCoauthor.setSpeaker(true);
                                    }
                                    publicationCoauthor.setPublication(publication);

                                    publicationCoauthorsRepo.save(publicationCoauthor);
                                } else {
                                    boolean chooser = true;
                                    for (PublicationCoauthor one : publicationCoauthorsRepo.findByPublication(publication)) {
                                        if (coauthorAffiliation.equals(one.getCoauthorAffiliation())) {
                                            chooser = false;
                                            break;
                                        }
                                    }
                                    if (chooser) {
                                        publicationCoauthor.setPlace(i);
                                        publicationCoauthor.setCoauthorAffiliation(coauthorAffiliation);
                                        publicationCoauthor.setPublication(publication);
                                        if (publication.getUser() != null && currentUser.equals(user)) {
                                            publicationCoauthor.setSpeaker(true);
                                        }
                                        publicationCoauthorsRepo.save(publicationCoauthor);
                                    }
                                }
                            }
                        }
                    } else {
                        return false;
                    }
                    return true;
                }
            }
        }
        return false;
    }

    public void publicationPermissionSave(Publication publication, String permissionName) {
        publication.setPermission(permissionName);
        publicationRepo.save(publication);
    }

    public void affiliationDelete(Long affiliationId) {
        coauthorAffiliationRepo.deleteAllByAffiliation_AffiliationId(affiliationId);
        affiliationRepo.deleteByAffiliationId(affiliationId);
    }
}
