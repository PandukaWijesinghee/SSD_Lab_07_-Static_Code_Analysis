import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.PreparedStatement;

@WebServlet(name = "Search", urlPatterns = {"/search"})
public class Search extends HttpServlet {

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        processRequest(request, response);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        processRequest(request, response);
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        try {
            String product_name = request.getParameter("product_name");

            Class.forName("com.mysql.jdbc.Driver");

            String dbPassword = System.getenv("DB_PASSWORD");

            String sql = "SELECT product_name, product_price " +
                    "FROM products WHERE product_name=?";

            try (Connection conn = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/webapp",
                    "root",
                    dbPassword
            );
                 PreparedStatement st = conn.prepareStatement(sql)) {

                st.setString(1, product_name);

                try (ResultSet rs = st.executeQuery()) {

                    while (rs.next()) {

                        String prod_name = rs.getString("product_name");
                        String prod_price = rs.getString("product_price");

                        out.println(prod_name);
                        out.println(prod_price);
                        out.println("<br/>");
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            out.close();
        }
    }
}