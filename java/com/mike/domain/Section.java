package com.mike.domain;

public enum Section {
    SECTION_1("Секция 1. Физическая мезомеханика материалов и структурно-неоднородных сред"),
    SECTION_2("Секция 2. Физика пластичности и прочности материалов"),
    SECTION_3("Секция 3. Моделирование поведения материалов на различных масштабах и компьютерный дизайн"),
    SECTION_4("Секция 4. Разработка перспективных конструкционных и функциональных материалов, передовые технологии их получения");

    private String section;

    Section(String string) {
        this.section = string;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }
}
