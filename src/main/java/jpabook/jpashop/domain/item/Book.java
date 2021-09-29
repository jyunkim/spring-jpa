package jpabook.jpashop.domain.item;

import jpabook.jpashop.dto.UpdateBookDto;
import lombok.Getter;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("B") // 기본값 = 엔티티 이름
@Getter
public class Book extends Item {

    private String author;
    private String isbn;

    @Override
    public void update(UpdateBookDto bookDto) {
        super.update(bookDto);
        author = bookDto.getAuthor();
        isbn = bookDto.getIsbn();
    }
}
