/**
 * @author wjw
 * @date 2020/6/6 0:52
 */
package com.leyou.item.controller;

import com.leyou.item.pojo.Brand;
import com.leyou.item.pojo.Category;
import com.leyou.item.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
//@CrossOrigin
@RequestMapping("category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 根据父节点id查询子节点
     * @param pid
     * @return
     */
//    @CrossOrigin
    @GetMapping("/list")
    public ResponseEntity<List<Category>> queryCategoriesByPid(@RequestParam(value = "pid", defaultValue = "0")Long pid){
        if (pid == null || pid < 0){
            //400:参数不合法
            return ResponseEntity.badRequest().build();
        }
        List<Category> categories = this.categoryService.queryCategoriesByPid(pid);
        if (CollectionUtils.isEmpty(categories)){
            //404：资源服务器未找到
            return ResponseEntity.notFound().build();
        }
        //200：查询成功
        return ResponseEntity.ok(categories);
        //500：服务器内部错误，因为本来就会返回500，所以我们不try/catch，也不用下面这句
//        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    @GetMapping("bid/{bid}")
    public ResponseEntity<List<Category>> queryCategoriesByBid(@PathVariable("bid") Long bid) {
        List<Category> categories = this.categoryService.queryCategoriesByBid(bid);
        if (CollectionUtils.isEmpty(categories)){
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(categories);
    }

    @GetMapping
    public ResponseEntity<List<String>> queryNamesByIds(@RequestParam("ids") List<Long> ids){
        List<String> names = this.categoryService.queryNameByIds(ids);
        if (CollectionUtils.isEmpty(names)){
            //404：资源服务器未找到
            return ResponseEntity.notFound().build();
        }
        //200：查询成功
        return ResponseEntity.ok(names);
    }
}
