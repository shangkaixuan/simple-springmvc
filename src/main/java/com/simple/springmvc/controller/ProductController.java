package com.simple.springmvc.controller;

import com.simple.springmvc.annotation.Controller;
import com.simple.springmvc.annotation.Quaifier;
import com.simple.springmvc.annotation.RequestMapping;
import com.simple.springmvc.domain.Product;
import com.simple.springmvc.service.IProductService;

/**
 * @author srh
 * @date 2019/10/23
 **/
@Controller(value = "productController")
@RequestMapping(value = "/product")
public class ProductController {

    @Quaifier(value = "productService")
    private IProductService productService;

    @RequestMapping(value = "/create")
    public void create() {
        Product product = productService.create();
        System.out.println(product.toString());
    }

}
