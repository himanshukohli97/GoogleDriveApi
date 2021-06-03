package com.googledrivelist.controller;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.googledrivelist.dto.FileItemDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


@Controller
public class HomepageController {

    private static final List<String> SCOPES = Arrays.asList(DriveScopes.DRIVE,
            "https://www.googleapis.com/auth/drive.install");
    private static final String USER_IDENTIFIER_KEY = "MY_DUMMY_USER";

    // private static final List<String> SCOPES =
    // Collections.singletonList(DriveScopes.DRIVE);
    private static HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    private static JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    @Value("${google.oauth.callback.uri}")
    private String CALLBACK_URI;

    @Value("${google.secret.key.path}")
    private Resource gdSecretKeys;

    @Value("${google.credentials.folder.path}")
    private Resource credentialsFolder;

    private GoogleAuthorizationCodeFlow flow;

    @PostConstruct
    public void init() throws Exception {
        GoogleClientSecrets secrets = GoogleClientSecrets.load(JSON_FACTORY,
                new InputStreamReader(gdSecretKeys.getInputStream()));
        flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY, secrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(credentialsFolder.getFile())).build();
    }

    @GetMapping(value = {"/"})
    public String showHomePage() throws Exception {
        boolean isUserAuthenticated = false;

        Credential credential = flow.loadCredential(USER_IDENTIFIER_KEY);
        if (credential != null) {
//            boolean tokenValid = true;
            boolean tokenValid = credential.refreshToken();
            if (tokenValid) {
                isUserAuthenticated = true;
            }
        }

        return isUserAuthenticated ? "dashboard.html" : "index.html";
    }

    @GetMapping(value = {"/googlesignin"})
    public void doGoogleSignIn(HttpServletResponse response) throws Exception {
        GoogleAuthorizationCodeRequestUrl url = flow.newAuthorizationUrl();
        String redirectURL = url.setRedirectUri(CALLBACK_URI).setAccessType("offline").build();
        response.sendRedirect(redirectURL);
    }

    @GetMapping(value = {"/oauth"})
    public String saveAuthorizationCode(HttpServletRequest request) throws Exception {
        String code = request.getParameter("code");
        if (code != null) {
            saveToken(code);

            return "dashboard.html";
        }

        return "index.html";
    }

    private void saveToken(String code) throws Exception {
        GoogleTokenResponse response = flow.newTokenRequest(code).setRedirectUri(CALLBACK_URI).execute();
        flow.createAndStoreCredential(response, USER_IDENTIFIER_KEY);
    }

    @GetMapping(value = {"/listfiles"}, produces = {"application/json"})
    public @ResponseBody
    List<FileItemDTO> listFiles() throws Exception {
        Credential cred = flow.loadCredential(USER_IDENTIFIER_KEY);

        Drive drive = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, cred)
                .setApplicationName("googledrivespringbootexample").build();

        List<FileItemDTO> responseList = new ArrayList<>();

        FileList fileList = drive.files().list()
//                .setQ("mimeType = 'application/vnd.google-apps.folder'")
                .setFields("files(id, name, thumbnailLink, webViewLink, webContentLink)")
                .execute();
        for (File file : fileList.getFiles()) {
            FileItemDTO item = new FileItemDTO();
            item.setId(file.getId());
            item.setName(file.getName());
            item.setThumbnailLink(file.getThumbnailLink());
            item.setWebViewLink(file.getWebViewLink());
            item.setWebContentLink(file.getWebContentLink());
            System.out.println("WebviewLink: " + file.getWebViewLink());
            System.out.println("WebContentLink: " + file.getWebContentLink());
            responseList.add(item);
        }

//        FileList fileList1 = drive.files().list().setQ("parents = '1eADupAt9jsB0SnB_14GQbUzfHJl_fSnJ'").execute();
        return responseList;
    }

    @GetMapping(value = {"/downloadFile/{fileId}"}, produces = {"application/json"})
    public @ResponseBody
    Message downloadFile(@PathVariable(name = "fileId") String fileId) throws Exception {

        Credential cred = flow.loadCredential(USER_IDENTIFIER_KEY);
        Drive drive = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, cred)
                .setApplicationName("googledrivespringbootexample").build();

        File file = drive.files().get(fileId).execute();
        System.out.println(file.getWebViewLink());
        System.out.println(file.getWebContentLink());


        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        drive.files().get(fileId).executeMediaAndDownloadTo(outputStream);

        FileOutputStream fileOutputStream = new FileOutputStream(file.getName());
        outputStream.writeTo(fileOutputStream);

        Message message = new Message();
        message.setMessage("File has been downloaded");
        return message;
    }

    class Message {
        private String message;

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

    }

}