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
import java.sql.Statement;

@WebServlet(name = "SearchPost", urlPatterns = {"/searchpost"})
public class SearchPost extends HttpServlet {

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

            try {
                Class.forName("com.mysql.jdbc.Driver");
                Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/webapp", "root", "mypassword");

                Statement st = conn.createStatement();
                String sql = "SELECT product_name, product_price FROM products WHERE product_name='" + product_name + "'";
                System.out.println(sql);
                ResultSet rs = st.executeQuery(sql);

                while (rs.next()) {
                    String prod_name = rs.getString("product_name");
                    String prod_price = rs.getString("product_price");
                    out.println(prod_name);
                    out.println(prod_price);
                    out.println("<br/>");
                }
                conn.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
        } finally {
            out.close();
        }
    }
}
