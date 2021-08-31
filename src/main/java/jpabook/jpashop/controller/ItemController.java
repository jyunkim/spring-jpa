package jpabook.jpashop.controller;

import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.service.ItemService;
import jpabook.jpashop.dto.UpdateBookDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @GetMapping("/items/new")
    public String createForm(Model model) {
        model.addAttribute("form", new BookForm());
        return "items/createItemForm";
    }

    @PostMapping("/items/new")
    public String create(BookForm form) {
        Book book = new Book();
        book.update(new UpdateBookDto(form));

        itemService.saveItem(book);
        return "redirect:/items";
    }

    @GetMapping("/items")
    public String list(Model model) {
        List<Item> items = itemService.findItems();
        model.addAttribute("items", items);
        return "items/itemList";
    }

    // PathVariable - 파라미터 변수 매핑
    @GetMapping("/items/{itemId}/edit")
    public String updateForm(@PathVariable("itemId") Long itemId, Model model) {
        Item item = itemService.findOne(itemId);

        if (item.getClass() == Book.class) {
            Book book = (Book) item;

            BookForm form = new BookForm();
            form.setId(book.getId());
            form.setName(book.getName());
            form.setPrice(book.getPrice());
            form.setStockQuantity(book.getStockQuantity());
            form.setAuthor(book.getAuthor());
            form.setIsbn(book.getIsbn());
            model.addAttribute("form", form);
        }
        return "items/updateItemForm";
    }

    // HTML form 전송 시에는 GET, POST만 지원
    @PostMapping("/items/{itemId}/edit")
    // model.addAttribute()를 쓰지 않아도 자동으로 추가됨
    // 기본값 - model.addAttribute("bookForm", form)
    // @ModelAttribute("form") -> model.addAttribute("form", form)
    public String update(@ModelAttribute("form") BookForm form) {
        // merge
//        Item book = new Book(form.getName(), form.getPrice(), form.getStockQuantity(),
//                form.getAuthor(), form.getIsbn());
//        book.setId(form.getId()); // Id가 있기 때문에 persist가 아닌 merge 호출
//
//        itemService.saveItem(book); // book - 준영속 엔티티

        // 변경 감지
//        itemService.updateItem(form.getId(), form.getName(), form.getPrice(), form.getStockQuantity());

        // DTO
        itemService.updateItem(new UpdateBookDto(form));
        return "redirect:/items";
    }
}
