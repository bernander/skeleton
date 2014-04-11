package com.bernander.mvc1;

import java.io.IOException;
import java.util.Locale;

import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.bernander.mvc1.GoogleDriveApiUtils.CodeExchangeException;
import com.bernander.mvc1.GoogleDriveApiUtils.NoRefreshTokenException;
import com.google.api.client.auth.oauth2.Credential;

/**
 * Handles requests for the application home page.
 */
@Controller
public class AuthorizationController {
	
//	private static final Logger logger = LoggerFactory.getLogger(AuthorizationController.class);
	
	@RequestMapping(value = "/authorization", method = RequestMethod.GET)
	public String home(Locale locale, Model model, HttpSession session,
			@RequestParam(value="code", required=false) String code,
			@RequestParam(value="error", required=false) String error,
			@RequestParam(value="state", required=true) String state)
	{
		Credential credential = null;
		try {
			credential = GoogleDriveApiUtils.getCredentialsFromGoogle(code, "getCredentialsFromGoogle");
			GoogleDriveApiUtils.setCredentials(session, credential);
		} catch (CodeExchangeException e) {
			e.printStackTrace();
		} catch (NoRefreshTokenException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		model.addAttribute("code", code);
		model.addAttribute("error", error);
		model.addAttribute("state", state);
		if (credential != null) {
			model.addAttribute("accessToken",  credential.getAccessToken());
			model.addAttribute("refreshToken", credential.getRefreshToken());
		}
//		return "authorization";
		return "redirect:/home";
	}	
}