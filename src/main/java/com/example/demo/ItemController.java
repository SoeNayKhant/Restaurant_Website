package com.example.demo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class ItemController {
	
	@Autowired
	private MemberRepository memberRepository;
	
	@Autowired
	private ItemRepository itemRepository;

	@Autowired
	private CategoryRepository categoryRepository;

	@Autowired
	private CartItemRepository cartItemRepo;

	@GetMapping("/items")
	public String viewItems(Model model,Principal principal) {

		// Get currently logged in user
		MemberDetails loggedInMember = (MemberDetails) SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal();
		int loggedInMemberId = loggedInMember.getMember().getId();// member id
		Member member=memberRepository.getReferenceById(loggedInMemberId);
		model.addAttribute("member",member);
		// Get shopping cart items added by this user
		// *Hint: You will need to use the method we added in the CartItemRepository
		List<CartItem> cartItemList = cartItemRepo.findAllByMemberId(loggedInMemberId);

		int count = 0;
		for (CartItem cartItem : cartItemList) {
			count++;
		}

		// Add the shopping cart total to the model
		model.addAttribute("count", count);

		Category category = new Category();
		model.addAttribute("category", category);
		List<Category> categoryList = categoryRepository.findAll();
		model.addAttribute("categoryList", categoryList);
		List<Item> itemList = itemRepository.findAll();// selcet * from category
		model.addAttribute("itemList", itemList);
		
		return "view_items";
	}

	@GetMapping("/itemadd")
	public String addItem(Model model, Principal principal) {
		int loggedInMemberId = 0;
		if (principal != null) {
			// Get currently logged in user
			MemberDetails loggedInMember = (MemberDetails) SecurityContextHolder.getContext().getAuthentication()
					.getPrincipal();
			loggedInMemberId = loggedInMember.getMember().getId();// member id
		}

		Member member = memberRepository.getReferenceById(loggedInMemberId);
		model.addAttribute("member", member);
		Item item = new Item();
		model.addAttribute("item", item);
		List<Category> categoryList = categoryRepository.findAll();
		model.addAttribute("categoryList", categoryList);
		return "add_item";
	}

//	@PostMapping("item/save")
//	public String saveItem(Item item) {
//		itemRepository.save(item);
//		return "redirect:/items";
//	}
	@PostMapping("/itemsave")
	public String saveItem(@Valid Item item, BindingResult bindingResult,
			@RequestParam("itemImage") MultipartFile imgFile,Model model, Principal principal) {
		int loggedInMemberId = 0;
		if (principal != null) {
			// Get currently logged in user
			MemberDetails loggedInMember = (MemberDetails) SecurityContextHolder.getContext().getAuthentication()
					.getPrincipal();
			loggedInMemberId = loggedInMember.getMember().getId();// member id
		}

		Member member = memberRepository.getReferenceById(loggedInMemberId);
		model.addAttribute("member", member);
		if (bindingResult.hasErrors()) {
			return "add_item";
		}
		String imageName = imgFile.getOriginalFilename();
		// set the image name in item object
		item.setImgName(imageName);
		// save the item obj to the db
		Item savedItem = itemRepository.save(item);
		try {
			// prepare the directory path
			String uploadDir = "uploads/items/" + savedItem.getId();
			Path uploadPath = Paths.get(uploadDir);
			// check if the upload path exists, if not it will be created
			if (!Files.exists(uploadPath)) {
				Files.createDirectories(uploadPath);
			}
			// prepare path for file
			Path fileToCreatePath = uploadPath.resolve(imageName);
			System.out.println("File path: " + fileToCreatePath);
			// copy file to the upload location
			Files.copy(imgFile.getInputStream(), fileToCreatePath, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "redirect:/items";
	}

	@GetMapping("/items{id}")
	public String viewSingleitem(@PathVariable("id") Integer id, Model model) {
		
		// Get currently logged in user
		MemberDetails loggedInMember = (MemberDetails) SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal();
		int loggedInMemberId = loggedInMember.getMember().getId();// member id
		Member member=memberRepository.getReferenceById(loggedInMemberId);
		model.addAttribute("member",member);
		
		List<CartItem> cartItemList = cartItemRepo.findAllByMemberId(loggedInMemberId);
		model.addAttribute("cartItemList", cartItemList);
		int count = 0;
		for (CartItem cartItem : cartItemList) {
			count++;
		}
		model.addAttribute("count", count);
		
		
		List<Category> categoryList = categoryRepository.findAll();
		model.addAttribute("categoryList", categoryList);
		
		Item item = itemRepository.getReferenceById(id);
		model.addAttribute("item", item);
		
		Category category = categoryRepository.getReferenceById(id);
		model.addAttribute("category", category);
		
		return "view_single_item";
	}

	@GetMapping("/itemedit{id}")
	public String editItem(@PathVariable("id") Integer id, Model model, Principal principal) {
		int loggedInMemberId = 0;
		if (principal != null) {
			// Get currently logged in user
			MemberDetails loggedInMember = (MemberDetails) SecurityContextHolder.getContext().getAuthentication()
					.getPrincipal();
			loggedInMemberId = loggedInMember.getMember().getId();// member id
		}

		Member member = memberRepository.getReferenceById(loggedInMemberId);
		model.addAttribute("member", member);
		Item item = itemRepository.getReferenceById(id);
		model.addAttribute("item", item);
		List<Category> categoryList = categoryRepository.findAll();
		model.addAttribute(categoryList);
		return "edit_item";

	}

	@PostMapping("/item/edit/{id}")
	public String saveUpdatedItem(@PathVariable("id") Integer id, Item item,
			@RequestParam("itemImage") MultipartFile imgFile) {
		String imageName = imgFile.getOriginalFilename();

		// get existing item from the database
		Item editItem = itemRepository.getReferenceById(id);

		if (imageName.isEmpty()) {
			// No new image selected, use the existing image name
			imageName = editItem.getImgName();
		}

		// set the image name in item object
		item.setImgName(imageName);

		// save the item obj to the db
		Item savedItem = itemRepository.save(item);
		try {
			// prepare the directory path
			String uploadDir = "uploads/items/" + savedItem.getId();
			Path uploadPath = Paths.get(uploadDir);

			// check if the upload path exists, if not it will be created
			if (!Files.exists(uploadPath)) {
				Files.createDirectories(uploadPath);
			}

			// prepare path for file
			Path fileToCreatePath = uploadPath.resolve(imageName);

			// copy file to the upload location if a new image is provided
			if (!imgFile.isEmpty()) {
				Files.copy(imgFile.getInputStream(), fileToCreatePath, StandardCopyOption.REPLACE_EXISTING);
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return "redirect:/items";
	}

	@GetMapping("/item/delete/{id}")
	public String deleteItem(@PathVariable("id") Integer id) {
		itemRepository.deleteById(id);
		return "redirect:/items";

	}

}
