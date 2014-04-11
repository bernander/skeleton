package com.bernander.mvc1;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.bernander.mvc1.entity.User;
import com.bernander.mvc1.service.UserService;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.Drive.Files;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

@Controller
public class EverythingController {
	
	@Autowired UserService userService;
	
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String getRoot(Locale locale, Model model, HttpSession session) throws IOException {
		model.addAttribute("someValue", "(From model)");
		return "root";
	}
	
	@RequestMapping(value = "/dashboard", method = RequestMethod.GET)
	public String getHome(Locale locale, Model model, HttpSession session) throws IOException {
		
//		// Ensure user has given us permission to access her files
//		Credential credential = GoogleDriveApiUtils.getCredentials(session);
//		if (credential == null) {
//			return "redirect:" + GoogleDriveApiUtils.getAuthorizationUrl("ojvind.bernander@gmail.com", "redirectFromHome");
//		}
//
//		// Get files		
//		Drive drive = GoogleDriveApiUtils.getDrive(credential);
//		FileWriter fw = new FileWriter("/tmp/out");
//		List<File> files = getAllFiles(drive, fw);
		

		// Get users
		User user = userService.getUser("moder");
		
		
		// Present
		model.addAttribute("someValue", user + " -- " + user.getId() + " -- " + user.getLogin()  + " -- " + user.getPassword()  + " -- " + user.getRole() );
		
		return "dashboard";
	}
	
    private static List<File> getAllFiles(Drive service, FileWriter fw) throws IOException {
    	final int totalSize = 5;
    	final int pageSize = 5; // 1000 is the largest value allowed
    	List<File> files = new ArrayList<File>();
		Files.List request = service.files().list().setMaxResults(pageSize);
		do {
		      try {
		        FileList fileList = request.execute(); // Gets a page of results
		        files.addAll(fileList.getItems());
		        for (File file : fileList.getItems()) {
		        	String title = file.getTitle();
		        	if (title != null) {
		        		title = title.replace(',', ' ');
		        	}
		        	fw.write(file.getFileExtension()
		        			+ ", " + file.getKind()
		        			+ ", " + file.getMimeType()
		        			+ ", " + (file.getWebContentLink() == null ? "  " : "wc")
		        			+ ", " + (file.getDownloadUrl()    == null ? "  " : "du")
		        			+ ", " + title
		        			+ "\n");
		        }
		        request.setPageToken(fileList.getNextPageToken());
		      } catch (IOException e) {
		        System.out.println("An error occurred: " + e);
		        request.setPageToken(null);
		      }
		    } while (request.getPageToken() != null && request.getPageToken().length() > 0 && files.size() < totalSize);

		    return files;
		  }
	
}
