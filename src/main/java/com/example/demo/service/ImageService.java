package com.example.demo.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class ImageService {
	
//	private static final String IMAGE_DIRECTORY = "/path/to/server/images/";
	private static final String projectPath = "/home/ec2-user/pepper/files/profile/";

	public static String getCurrentDateFormatted(String pattern) {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
		return simpleDateFormat.format(new Date());
	}
	
	public byte[] downloadImage(String imageUrl) throws Exception {
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<byte[]> response = restTemplate.getForEntity(imageUrl, byte[].class);
		
		if (response.getStatusCode().is2xxSuccessful()) {
			return response.getBody();
		} else {
			throw new Exception("Failed to download image");
		}
	}

	public String saveImageToServer(byte[] imageContent, String email) throws IOException {
		String imagePath = projectPath + getCurrentDateFormatted("yyyy/MM/dd")+ email + ".jpg";
		try (FileOutputStream fos = new FileOutputStream(imagePath)) {
			fos.write(imageContent);
		}
		return imagePath;
	}
}