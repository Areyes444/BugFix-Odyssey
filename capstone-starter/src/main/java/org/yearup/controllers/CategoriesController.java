package org.yearup.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.yearup.data.CategoryDao;
import org.yearup.data.ProductDao;
import org.yearup.models.Category;
import org.yearup.models.Product;

import java.sql.SQLException;
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
    @PreAuthorize("permitAll()")
    @GetMapping("/{id}")
    public Category getById(@PathVariable int id)
    {
        // get the category by id
        try
        {
            Category category = categoryDao.getById(id);

            if (category == null){
                throw new ResponseStatusException(HttpStatus.NOT_FOUND);
            }
            return category;

        }catch (Exception ex)
        {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Oops..Failed to find Category by id");
        }
    }

    // the url to return all products in category 1 would look like this
    // https://localhost:8080/categories/1/products
    @GetMapping("{categoryId}/products")
    @PreAuthorize("permitAll()")
    public List<Product> getProductsById(@PathVariable int categoryId)
    {
        // get a list of product by categoryId
        try {
            if (productDao.listByCategoryId(categoryId).isEmpty())
                throw new ResponseStatusException(HttpStatus.NOT_FOUND);
            else {
                return productDao.listByCategoryId(categoryId);
            }
        }catch (Exception e){
            throw new RuntimeException(e);
    }
    }

    // add annotation to call this method for a POST action
    // add annotation to ensure that only an ADMIN can call this function
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Category addCategory(@RequestBody Category category)
    {
        var newCategory = categoryDao.create(category);
        return newCategory;
    }

    // add annotation to call this method for a PUT (update) action - the url path must include the categoryId
    // add annotation to ensure that only an ADMIN can call this function
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Category> updateCategory(@PathVariable int id, @RequestBody Category category)
    {
        Category inventoryCategory = categoryDao.getById(id);
        if ((inventoryCategory != null))
        {
            category.setCategoryId(id);
            categoryDao.update(id, category);
            return ResponseEntity.ok(category);
        }else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

    }


    @DeleteMapping("{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ResponseStatus(value=HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable int id)
    {
        // delete the category by id
        try
        {
            Category category = categoryDao.getById(id);

            if(category == null)
                throw new ResponseStatusException(HttpStatus.NOT_FOUND);

            categoryDao.delete(id);
        }
        catch(Exception ex)
        {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.");
        }
    }

}
