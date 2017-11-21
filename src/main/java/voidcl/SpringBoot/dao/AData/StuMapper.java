package voidcl.SpringBoot.dao.AData;

import voidcl.SpringBoot.entity.Student;

import java.util.List;

public interface StuMapper {
    void insertStu(Student data);

    List<Student> selectAllStu();

    void updateStuName(String name);

    Student selectById(String id);

    void updateById(String name);

    List<String> selectFake();
}
