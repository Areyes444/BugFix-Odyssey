package org.yearup.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.yearup.data.CategoryDao;
import org.yearup.data.ProductDao;
import org.yearup.models.Category;
import org.yearup.models.Product;

import java.util.List;


// add the annotations to make this a REST controller
// add the annotation to make this controller the endpoint for the following url
    // http://localhost:8080/categories
// add annotation to allow cross site origin requests


@RestController
@RequestMapping("/categories")
@CrossOrigin

public class CategoriesController
{
    private CategoryDao categoryDao;
    private ProductDao productDao;


    // create an Autowired controller to inject the categoryDao and ProductDao
    @Autowired
    public CategoriesController(CategoryDao categoryDao, ProductDao productDao)
    {
        this.categoryDao = categoryDao;
        this.productDao = productDao;
    }

    // add the appropriate annotation for a get action
    @GetMapping("")
    public List<Category> getAll()
    {
        // find and return all categories
        try
        {
            return categoryDao.getAllCategories();
        }catch (Exception ex)
        {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops...Failed to load Categories");
        }
    }

    // add the appropriate annotation for a get action
    @GetMapping("/{id}")
    public Category getById(@PathVariable int id)
    {
        // get the category by id
        try
        {
            return categoryDao.getById(id);

        }catch (Exception ex)
        {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Oops..Failed to find Category by id");
        }
    }

    // the url to return all products in category 1 would look like this
    // https://localhost:8080/categories/1/products
    @GetMapping("{categoryId}/products")
    public List<Product> getProductsById(@PathVariable int categoryId)
    {
        // get a list of product by categoryId
        try
        {
            return productDao.listByCategoryId(categoryId);
        }catch (Exception ex)
        {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops.. Failed to get list of products");
        }
    }

    // add annotation to call this method for a POST action
    // add annotation to ensure that only an ADMIN can call this function
    @PostMapping("")
    @PreAuthorize("hasRole('Role_ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public Category addCategory(@RequestBody Category category)
    {
        // insert the category
        try
        {
            return categoryDao.create(category);
        }
        catch (Exception ex)
        {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops...Can't add Category");
        }
    }

    // add annotation to call this method for a PUT (update) action - the url path must include the categoryId
    // add annotation to ensure that only an ADMIN can call this function
    @PutMapping("{id}")
    @PreAuthorize("hasRole('Role_ADMIN')")
    public void updateCategory(@PathVariable int id, @RequestBody Category category)
    {
        // update the category by id
        try
        {
            categoryDao.update(id, category);
        }
        catch (Exception ex)
        {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Oops..Failed to update Category");
        }
    }


    // add annotation to call this method for a DELETE action - the url path must include the categoryId
    // add annotation to ensure that only an ADMIN can call this function
    @DeleteMapping("{id}")
    @PreAuthorize("hasRole('Role_ADMIN')")
    public void deleteCategory(@PathVariable int id)
    {
        try
        {
            categoryDao.delete(id);
        }catch (Exception ex)
        {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops..can't delete Category");
        }
        // delete the category by id
    }
}
