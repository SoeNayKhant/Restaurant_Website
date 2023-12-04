package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.security.Principal;
import java.util.List;

import javax.validation.Valid;

@Controller
public class CategoryController {

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private CategoryRepository categoryRepository;

	@Autowired
	private ItemRepository itemRepository;

	@Autowired
	private CartItemRepository cartItemRepo;

	@GetMapping("/categories")
	public String viewCategories(Model model, Principal principal) {
		int loggedInMemberId = 0;
		if (principal != null) {
			// Get currently logged in user
			MemberDetails loggedInMember = (MemberDetails) SecurityContextHolder.getContext().getAuthentication()
					.getPrincipal();
			loggedInMemberId = loggedInMember.getMember().getId();// member id
		}

		Member member = memberRepository.getReferenceById(loggedInMemberId);
		model.addAttribute("member", member);
		List<Category> categoryList = categoryRepository.findAll();// selcet * from category
		model.addAttribute("categoryList", categoryList);
		return "view_categories";
	}

	@GetMapping("/categoryadd")
	public String addCategory(Model model, Principal principal) {
		int loggedInMemberId = 0;
		if (principal != null) {
			// Get currently logged in user
			MemberDetails loggedInMember = (MemberDetails) SecurityContextHolder.getContext().getAuthentication()
					.getPrincipal();
			loggedInMemberId = loggedInMember.getMember().getId();// member id
		}

		Member member = memberRepository.getReferenceById(loggedInMemberId);
		model.addAttribute("member", member);
//		Category category=new Category();
//		model.addAllAttributes("category",category);
		model.addAttribute("category", new Category());
		return "add_category";
	}

	@PostMapping("/categorysave")
	public String saveCategory(@Valid Category c, BindingResult bindingResult, Model model, Principal principal) {
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
			return "add_category";
		}
		categoryRepository.save(c);
		return "redirect:/categories";
	}

	@GetMapping("/categories{id}")
	public String viewSingleitem(@PathVariable("id") Integer id, Model model) {

		// Get currently logged in user
		MemberDetails loggedInMember = (MemberDetails) SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal();
		int loggedInMemberId = loggedInMember.getMember().getId();// member id
		Member member = memberRepository.getReferenceById(loggedInMemberId);
		model.addAttribute("member", member);

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
		List<Item> itemList = itemRepository.findAllByCategoryId(id);
		model.addAttribute("itemList", itemList);
		return "view_single_category";
	}

	@GetMapping("/categoryedit{id}")
	public String editCategory(@PathVariable("id") Integer id, Model model, Principal principal) {
		int loggedInMemberId = 0;
		if (principal != null) {
			// Get currently logged in user
			MemberDetails loggedInMember = (MemberDetails) SecurityContextHolder.getContext().getAuthentication()
					.getPrincipal();
			loggedInMemberId = loggedInMember.getMember().getId();// member id
		}

		Member member = memberRepository.getReferenceById(loggedInMemberId);
		model.addAttribute("member", member);
		Category category = categoryRepository.getReferenceById(id);
		model.addAttribute("category", category);
		return "edit_category";

	}

	@PostMapping("/category/edit/{id}")
	public String saveUpdatedCategoy(Category category) {
		categoryRepository.save(category);
		return "redirect:/categories";
	}

	@GetMapping("/category/delete/{id}")
	public String deleteCategory(@PathVariable("id") Integer id) {
		categoryRepository.deleteById(id);
		return "redirect:/categories";

	}

}
