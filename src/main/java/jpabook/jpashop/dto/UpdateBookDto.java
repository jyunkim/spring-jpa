package jpabook.jpashop.dto;

import jpabook.jpashop.controller.BookForm;
import lombok.Getter;

@Getter
public class UpdateBookDto {

    private Long id;

    private String name;
    private int price;
    private int stockQuantity;

    private String author;
    private String isbn;

    public UpdateBookDto(String name, int price, int stockQuantity, String author, String isbn) {
        this.name = name;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.author = author;
        this.isbn = isbn;
    }

    public UpdateBookDto(BookForm form) {
        id = form.getId();
        name = form.getName();
        price = form.getPrice();
        stockQuantity = form.getStockQuantity();
        author = form.getAuthor();
        isbn = form.getIsbn();
    }
}
