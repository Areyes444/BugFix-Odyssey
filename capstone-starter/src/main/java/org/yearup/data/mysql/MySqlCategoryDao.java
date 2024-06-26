package org.yearup.data.mysql;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.yearup.data.CategoryDao;
import org.yearup.models.Category;
import org.yearup.models.Product;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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
                    from easyshop.categories;
                    """;

            Statement statement = connection.createStatement();

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
                    SELECT name
                    , description
                    from easyshop.categories
                    where category_id = ?;
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
                    insert into easyshop.categories(name, description)
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
                    update easyshop.categories
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
    public void delete(int categoryId) throws SQLException
    {
        try(Connection connection = getConnection())
        {
            String sqlProducts = """
                    update easyshop.products
                    set category_id = null
                    where category_id = ?;
                    """;

            PreparedStatement statementProducts = connection.prepareStatement(sqlProducts);
            statementProducts.setInt(1, categoryId);
            statementProducts.executeUpdate();

            String sql = """
                    delete from easyshop.categories
                    where catergory_id = ?;
                    """;

            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, categoryId);

            statement.executeUpdate();
        }
        catch (SQLException e)
        {
            throw e;
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
