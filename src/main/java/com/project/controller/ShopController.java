package com.project.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.project.module.dto.ShopDto;
import com.project.payload.ApiResponse;
import com.project.payload.FileUploadResponse;
import com.project.services.ShopService;
import com.project.services.UserService;

import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/collegeazy/store")
@CrossOrigin(origins = "http://localhost:3000")
public class ShopController {
	
	@Autowired
	private ShopService shopService;
	
	@Autowired
	private UserService userService;
	
	private final String directory = "C:\\Users\\pande\\Documents\\workspace-spring-tool-suite-4-4.16.0.RELEASE\\Project-2\\shop\\";
	
	
	@PostMapping("/addItem/{category}")
    public ResponseEntity<FileUploadResponse>  uploadFile(@RequestParam("file") MultipartFile file,
                           @RequestParam("directory") String directory,
                           @PathVariable("category") String category,
                           @RequestParam("description") String description,
                           @RequestParam("enrollment") String enrollment,
                           @RequestParam("name") String name,
                           @RequestParam("price") String price,
                           @RequestParam("contact") String contact,
                           @RequestParam("title") String title) throws IOException {
		if(directory==null || category==null || description==null || title ==null ||
				enrollment == null || name==null || price==null || contact==null)
			// return message : seems like one or more field(s) is/are empty
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		if(userService.findByEnrollment(enrollment)==null) {					
			// return message : seems like you haven't registered
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        long size = file.getSize();
        String filecode = shopService.saveImage(fileName, category, description, name, price, enrollment, title, contact, file);
       
        
        FileUploadResponse response = new FileUploadResponse();
        response.setFileName(fileName);
        response.setSize(size);
        response.setDownloadUri(filecode);
       
        return new ResponseEntity<>(response, HttpStatus.OK);		       
    }
	
	@GetMapping("/getImage/{category}/{filename:.+}")
 	public void getImage(@PathVariable String category, @PathVariable String filename, HttpServletResponse response) throws IOException {
 	    if(category == null || filename == null)
 	    	return;// new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		
		String filePath = directory + category + "\\" + filename;
 	    Path path = Paths.get(filePath);

 	    if (Files.exists(path)) {
 	        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
 	        response.setHeader("Content-Disposition", "inline; filename=" + filename);

 	        try {
 	            Files.copy(path, response.getOutputStream());
 	            response.getOutputStream().flush();
 	        } catch (IOException ex) {
 	            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
 	            response.getWriter().write("Failed to retrieve image: " + ex.getMessage());
 	            response.getWriter().flush();
 	        }
 	    } else {
 	        throw new RuntimeException("File not found");
 	    }
 	}
	
	@DeleteMapping("/removeItem/{enrollment}/{uid}")
	public ResponseEntity<ApiResponse> removeItem(@PathVariable String enrollment, @PathVariable Integer uid){
			// below authorization is not working
		System.out.println(shopService.findEnrollmentByuid(uid));
		System.out.println(enrollment);
		if(userService.findByEnrollment(shopService.findEnrollmentByuid(uid)).toString().equals(enrollment)) {					
			// return message : seems like you're trying delete someone else item
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		this.shopService.deleteItem(uid);
		return new ResponseEntity<ApiResponse>(new ApiResponse("Item deleted successfully", true),HttpStatus.OK);

	}
	
	@GetMapping("/items")
	public ResponseEntity<List<ShopDto>> getAllItems(){
		List<ShopDto> items = this.shopService.getAllItem();

	    for (ShopDto item : items) {
	        String imagePath = "/getImage/" + item.getCategory() + "/" + item.getImagePath();
	        item.setImagePath(imagePath);
	    }
		return ResponseEntity.ok(this.shopService.getAllItem());
	}
	
	@GetMapping("/items/{category}")
	public ResponseEntity<List<ShopDto>> getItemByCategory(@PathVariable String category, HttpServletResponse response) throws IOException{
		return ResponseEntity.ok(this.shopService.getItemByCategory(category));
	}
	
	@PutMapping("/updateItem/{id}")
	public ResponseEntity<ShopDto> updateItem(@RequestBody ShopDto shopDto,@PathVariable int id){
			// not working properly
		return ResponseEntity.ok(this.shopService.updateItem(shopDto, id));
	}
	
}
