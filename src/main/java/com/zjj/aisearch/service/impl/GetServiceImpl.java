package com.zjj.aisearch.service.impl;

import com.zjj.aisearch.mapper.DocumentMapper;
import com.zjj.aisearch.mapper.GetMapper;
import com.zjj.aisearch.mapper.MovieMapper;
import com.zjj.aisearch.model.Todo;
import com.zjj.aisearch.pojo.dto.DocumentDTO;
import com.zjj.aisearch.pojo.dto.MovieDTO;
import com.zjj.aisearch.service.GetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @program: AISearch
 * @description: get请求service层
 * @author: zjj
 * @create: 2019-12-04 20:49:08
 **/
@Service
public class GetServiceImpl implements GetService {
    @Autowired
    private GetMapper getMapper;

    @Autowired
    private MovieMapper movieMapper;

    @Autowired
    private DocumentMapper documentMapper;

    @Override
    public List<Todo> getTodoList(Map<String, String> map) {

        return getMapper.getTodoList(map);
    }

    @Override
    public List<MovieDTO> getMovieDTOList() {
        return movieMapper.selectAll();
    }

    @Override
    public List<DocumentDTO> getDocumentDTOList() {
        return documentMapper.selectAll();
    }

    @Override
    public Integer getDocumentDTOByName(DocumentDTO documentDTO) {
        return documentMapper.selectCount(documentDTO);
    }


}
