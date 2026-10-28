package com.example.crawler.repository;

import com.example.crawler.entity.ContactData;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContactDataRepository extends JpaRepository<ContactData, Long> {

    // Поиск по типу контакта (например, только EMAIL) с пагинацией
    Page<ContactData> findByContactType(String contactType, Pageable pageable);

    // Поиск по частичному совпадению значения (например, домен "@netology.ru")
    Page<ContactData> findByContactValueContainingIgnoreCase(String keyword, Pageable pageable);

    // Получение всех записей (понадобится для parallelStream сортировки)
    List<ContactData> findAll();
}