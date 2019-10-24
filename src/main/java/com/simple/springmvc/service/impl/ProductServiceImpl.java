package com.simple.springmvc.service.impl;

import com.simple.springmvc.annotation.Service;
import com.simple.springmvc.domain.Product;
import com.simple.springmvc.service.IProductService;

/**
 * @author srh
 * @date 2019/10/23
 **/
@Service(value = "productService")
public class ProductServiceImpl implements IProductService {

    public Product create() {
        return new Product(1, "橡皮泥", 1.5D);
    }
}
