package com.xjz.gulimall.search.web;

import com.xjz.gulimall.search.dto.SearchParam;
import com.xjz.gulimall.search.service.MallSearchService;
import com.xjz.gulimall.search.vo.SearchResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;

@Controller
@Slf4j
public class SearchController {
    @Autowired
    private MallSearchService mallSearchService;
    @GetMapping("/list.html")
    public String list(SearchParam searchParam, Model model, HttpServletRequest httpServletRequest){
        // 设置默认页码
        if(searchParam.getPageNum() == null){
            searchParam.setPageNum(1);
        }
        String queryString = httpServletRequest.getQueryString();
        searchParam.setOldQueryString(queryString);
        SearchResult result=mallSearchService.search(searchParam);
        if(queryString!=null&&queryString.startsWith("&"))
        {
            queryString=queryString.substring(1);
        }
        model.addAttribute("result",result);
        model.addAttribute("queryString",queryString);
        return "list";
    }
}
