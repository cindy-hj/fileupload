package com.example.controller;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.example.dto.FileDto;

@Controller
public class MainController {
	@GetMapping("fileupload")
	public String fileupload(Model model) {
		return "fileupload";
	}
	
	@PostMapping("/upload")
	public String upload(@RequestParam MultipartFile[] uploadfile, Model model) 
			throws IllegalStateException, IOException{
		List<FileDto> list = new ArrayList<>();
		for(MultipartFile file : uploadfile) {
			if(!file.isEmpty()) {
				FileDto dto = new FileDto(UUID.randomUUID().toString(),
						file.getOriginalFilename().replaceAll("[^ㄱ-ㅎㅏ-ㅣ가-힣a-zA-z0-9.]", ""),
						file.getContentType());
				list.add(dto);
				
				File newFileName = new File(dto.getUuid()+"_"+dto.getFileName());
				file.transferTo(newFileName);
			}
		}
		model.addAttribute("files",list);
		return "filedownload";
	}
	
	@Value("${spring.servlet.multipart.location}")
	String filePath;
	
	@GetMapping("/download")
	public ResponseEntity<Resource> download(@ModelAttribute FileDto dto) throws IOException {
		Path path = Paths.get(filePath+"/"+dto.getUuid()+"_"+dto.getFileName());
		String contentType = Files.probeContentType(path);
		
		HttpHeaders headers = new HttpHeaders();
		
		headers.setContentDisposition(ContentDisposition.builder("attachment")
				.filename(dto.getFileName(), StandardCharsets.UTF_8)
				.build());
		headers.add(HttpHeaders.CONTENT_TYPE, contentType);
		
		Resource resource = new InputStreamResource(Files.newInputStream(path));
		return new ResponseEntity<Resource>(resource, headers, HttpStatus.OK);
	}
	
	@GetMapping("/display")
	public ResponseEntity<Resource> display(@ModelAttribute FileDto dto) throws IOException {
		String path = "C:\\Temp\\upload\\";
		String folder = "";
		
		Resource resource = new FileSystemResource(path + folder + dto.getUuid()+"_"+dto.getFileName());
		if(!resource.exists())
			return new ResponseEntity<Resource>(HttpStatus.NOT_FOUND);
		HttpHeaders header = new HttpHeaders();
		Path filePath = null;
		try {
			filePath = Paths.get(path + folder + dto.getUuid() + "_" + dto.getFileName());
			header.add("Content-type", Files.probeContentType(filePath));
		} catch(IOException e) {
			e.printStackTrace();
		}
		return new ResponseEntity<Resource>(resource, header, HttpStatus.OK);
	}
}
