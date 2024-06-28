# BugFix-Odyssey

##Project Summary
In this project I had to work on fixing some bugs that where not allowing certain Features work such as the Categories page to load up and as well as the price Range not to work. The web site included Spring Boot API and MySQL database that we have created in order to retrieve all the data. 

##DataBase Template 

![dataBase Bug Fix Template](https://github.com/Areyes444/BugFix-Odyssey/assets/166452594/3d5dfac3-bccd-4181-b630-bca059d2e81f)

##DataBase Created
Once we are able to create and run the Data we need for our project

![dataBase created](https://github.com/Areyes444/BugFix-Odyssey/assets/166452594/e3eafe30-95c4-4d00-8a01-7cfc6a487c8f)

##Phase 1 
In phase 1) I had to implement the code for each function and add the propper annotations in the controller in where only administrators are allowed to insert, update, and delete a category. As well for the MySqlCategoriesDao I needed to implement the functions as well. 

##CategoriesController
```java

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

    // appropriate annotation for a get action
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

    // appropriate annotation for a get action
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


    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Category addCategory(@RequestBody Category category)
    {
        var newCategory = categoryDao.create(category);
        return newCategory;
    }

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

```

##MySqlCategoriesDao

```java

@Component
public class MySqlCategoryDao extends MySqlDaoBase implements CategoryDao
{

    @Autowired
    public MySqlCategoryDao(DataSource dataSource)
    {
        super(dataSource);
    }

    @Override
    public List<Category> getAllCategories()
    {
        List<Category> categories = new ArrayList<>();
        try(Connection connection = getConnection())
        {
            String sql = """
                    SELECT category_id
                    , name
                    , description
                    FROM categories;
                    """;

            PreparedStatement statement = connection.prepareStatement(sql);

            ResultSet row = statement.executeQuery(sql);

            while(row.next())
            {
                Category category = mapRow(row);
                categories.add(category);
            }
        }
        catch (SQLException e)
        {

        }
        return categories;

    }

    @Override
    public Category getById(int categoryId)
    {
        try(Connection connection = getConnection())
        {
            String sql = """
                    SELECT category_id
                    , name
                    , description
                    FROM categories
                    WHERE category_id = ?;
                    """;

            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1,categoryId);

            ResultSet row = statement.executeQuery();

            if (row.next())
            {
                return mapRow(row);
            }
        }
        catch (SQLException e)
        {
        }
        return null;
    }


    @Override
    public Category create(Category category)
    {
        int newID = 0;

        try(Connection connection = getConnection())
        {
            String sql = """
                    insert into categories(name, description)
                    values(?,?);
                    """;
            PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            statement.setString(1, category.getName());
            statement.setString(2, category.getDescription());

            statement.executeUpdate();

            ResultSet generatedKeys = statement.getGeneratedKeys();
            generatedKeys.next();
            newID = generatedKeys.getInt(1);
        }
        catch (SQLException e)
        {
        }
        return  getById(newID);
    }

    @Override
    public void update(int categoryId, Category category)
    {
        try(Connection connection = getConnection())
        {
            String sql = """
                    update categories
                    set name = ?
                    , description = ?
                    where category_id = ?;
                    """;

            PreparedStatement statement = connection.prepareStatement(sql);

            statement.setString(1, category.getName());
            statement.setString(2, category.getDescription());
            statement.setInt(3,categoryId);

            statement.executeUpdate();
        }
        catch (SQLException e)
        {

        }
    }

    @Override
    public void delete(int categoryId)
    {
        try(Connection connection = getConnection())
        {

            String sql = """
                    delete from categories
                    where category_id = ?;
                    """;

            try(PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, categoryId);
                statement.executeUpdate();
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    private Category mapRow(ResultSet row) throws SQLException
    {
        int categoryId = row.getInt("category_id");
        String name = row.getString("name");
        String description = row.getString("description");

        Category category = new Category()
        {{
            setCategoryId(categoryId);
            setName(name);
            setDescription(description);
        }};

        return category;
    }

}

```

##Website 

Once that bug has been fixed I am able to display all the Categories 

![image](https://github.com/Areyes444/BugFix-Odyssey/assets/166452594/2374bae6-a423-41db-9e8e-689b2443a8aa)

##Phase 2

I had to fix the min price and maximum price since it was not displaying certain products. I had to as well Fix the issue of not having a product duplicate once it has been updated.

I had to create a method that gave the website the ability to display the prices and well as create a way for the price range to change when a product has been updated if is more than the max range and set that as the new max range price to prevent from manually always updating the max price.

```java
@Component
public class MySqlProductDao extends MySqlDaoBase implements ProductDao
{
    public MySqlProductDao(DataSource dataSource)
    {
        super(dataSource);
    }

    @Override
    public List<Product> search(Integer categoryId, BigDecimal minPrice, BigDecimal maxPrice, String color) {
        List<Product> products = new ArrayList<>();

        String sql = "SELECT * FROM products " +
                "WHERE (category_id = ? OR ? = -1) " +
                "  AND (price >= ? OR ? IS NULL) " +
                "  AND (price <= ? OR ? IS NULL) " +
                "  AND (color = ? OR ? = '') ";

        categoryId = categoryId == null ? -1 : categoryId;
        minPrice = minPrice == null ? BigDecimal.ZERO : minPrice;
        color = color == null ? "" : color;

        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, categoryId);
            statement.setInt(2, categoryId);
            statement.setBigDecimal(3, minPrice);
            statement.setBigDecimal(4, minPrice);
            if (maxPrice != null)
            {
                statement.setBigDecimal(5, maxPrice);
                statement.setBigDecimal(6, maxPrice);
            } else
            {
                BigDecimal reasonableMaxPrice = getMaxPriceFromProducts(connection);
                statement.setBigDecimal(5, reasonableMaxPrice);    //creating a maxprice that is reasonable as maxprice
                statement.setBigDecimal(6, reasonableMaxPrice);
            }
            statement.setString(7, color);
            statement.setString(8, color);

            ResultSet row = statement.executeQuery();

            while (row.next()) {
                Product product = mapRow(row);
                products.add(product);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return products;
    }

    private BigDecimal getMaxPriceFromProducts(Connection connection) throws SQLException
    {
        String sql = """
                SELECT MAX(price) AS max_price
                 FROM products
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql))
        {
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next())
            {
                BigDecimal maxPrice = resultSet.getBigDecimal("max_price");
                if (maxPrice == null)
                {
                    return BigDecimal.ZERO;
                }
                return maxPrice;
            } else {
                return BigDecimal.ZERO;
            }
        }
    }
```

In this method I had to fix the issue on why the products seemed to duplicate after being updating. The issue was that the method was creating it as a new one oppose to actually just updating it.

```java
@PutMapping("{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void updateProduct(@PathVariable int id, @RequestBody Product product)
    {
        try
        {
            productDao.update(id,product);
        }
        catch(Exception ex)
        {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.");
        }
    }
```

##The website now can display with price range 

![websitePriceRnage](https://github.com/Areyes444/BayBitesDeli/assets/166452594/45541850-33ec-4abd-8233-600e11c41e87)
