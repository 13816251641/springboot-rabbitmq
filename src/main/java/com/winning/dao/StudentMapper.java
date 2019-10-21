package com.winning.dao;

import com.winning.entity.Student;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

@Repository
public interface StudentMapper extends Mapper<Student> {

    public void insertWithoutId(Student student);

}