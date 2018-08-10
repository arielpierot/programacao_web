/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package web;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author arielpierot
 */
@WebServlet(name = "ComprovanteSolicitacao", urlPatterns = {"/ComprovanteSolicitacao"})
public class ComprovanteSolicitacao extends HttpServlet {

    static final String JDBC_DRIVER = "com.mysql.jdbc.Driver"; 
    //static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    static final String DATABASE_URL = "jdbc:mysql://localhost:3306/lista1?autoReconnect=true&useSSL=false";

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("text/html;charset=UTF-8");
        String saida = " ";
        Connection connection = null;
        Statement statement = null;
        Statement statement_usuario = null;
        
        String matricula = request.getParameter("matricula");
                
        StringBuilder sb= new StringBuilder();
        String turmas = "";

        String[] val=request.getParameterValues("turma_id[]");  
        for(int i=0;i<val.length;i++){  
         //printval[i];
         sb.append( "'"+val[i]+"'," );
        }

        turmas = sb.toString();
        turmas = turmas.substring(0, turmas.length()-1);
        
        try (PrintWriter out = response.getWriter()) {            
            Class.forName(JDBC_DRIVER); // carrega classe de driver do banco de dados
                // estabelece conexao com o banco de dados
            connection = DriverManager.getConnection(DATABASE_URL, "root", "proesc");
             // cria Statement para consultar banco de dados
            statement = connection.createStatement();
            statement_usuario = connection.createStatement();
          
            // consulta o banco de dados 
            statement.executeQuery("USE lista1;");
            ResultSet resultSet = statement.executeQuery("SELECT turmas.id, turmas.codigo, disciplinas.nome as disciplina, disciplinas.carga_horaria, turmas.horario_id_1, turmas.horario_id_2, turmas.horario_id_3  FROM turmas LEFT JOIN disciplinas ON disciplinas.id = turmas.disciplina_id WHERE turmas.id IN ("+ 
                    turmas +
             ") ORDER BY nome;");       

            while(resultSet.next()){
                
                String horario_1 = data_formatada(resultSet.getString("horario_id_1"));         
                String horario_2 = data_formatada(resultSet.getString("horario_id_2"));
                String horario_3 = data_formatada(resultSet.getString("horario_id_3"));
                
                saida = saida +"<tr>\n"
                       +"<td>" +resultSet.getString("codigo")+"</td>\n"
                       +"<td>" +resultSet.getString("disciplina")+"</td>\n"
                       +"<td>" +resultSet.getString("carga_horaria")+"</td>\n"
                       +"<td>" +horario_1+"</td>\n"
                       +"<td>" +horario_2+"</td>\n"
                       +"<td>" +horario_3+"</td>\n"
                       +"</tr>\n";
            }
            
            statement_usuario.executeQuery("USE lista1;");
            ResultSet resultSet_usuario = statement_usuario.executeQuery("SELECT alunos.nome, alunos.matricula, cursos.nome as curso FROM alunos LEFT JOIN cursos ON cursos.id = alunos.curso_id WHERE matricula like '"+ matricula +"' ");

            String aluno_matricula = null;
            String aluno_nome = null;
            String aluno_curso = null;
            
            if(resultSet_usuario.next())
            {
                aluno_matricula = resultSet_usuario.getString("matricula");
                aluno_nome = resultSet_usuario.getString("nome");
                aluno_curso = resultSet_usuario.getString("curso");
            }
            
            String conteudo = 
                           "          <div class='row'>\n" +
                           "            <div class='col-md-7'>\n" +
                           "              <div class='card'>\n" +
                           "                <div class='card-header card-header-primary'>\n" +
                           "                  <h4 class='card-title'>Solicitação de Matrícula</h4>\n" +
                           "                  <p class='card-category'>As turmas selecionadas estão abaixo</p>\n" +
                           "                </div>\n" +
                           "                <div class='card-body'>" +
                           "                  <form action='SelecionaTurmas' method='get'>\n" +
                           "                    <div class='row'>\n" +
                           "                      <div class=\"table-responsive\">\n" +
                           "                    <table class=\"table\">\n" +
                           "                      <thead class=\" text-primary\">\n" +
                           "                        <th width='10%'>\n" +
                           "                          Codigo\n" +
                           "                        </th>\n" +
                           "                        <th>\n" +
                           "                          Disciplina\n" +
                           "                        </th>\n" +
                           "                        <th>\n" +
                           "                          Carga Horária\n" +
                           "                        </th>\n" +
                           "                        <th width='20%'>\n" +
                           "                          Horário N1\n" +
                           "                        </th>\n" +
                           "                        <th width='20%'>\n" +
                           "                          Horário N2\n" +
                           "                        </th>\n" +
                           "                        <th width='20%'>\n" +
                           "                          Horário N3\n" +
                           "                        </th>\n" +
                           "                      </thead>\n" +
                           "                      <tbody>\n" + saida +
                           "                      </tbody>\n" +
                           "                    </table>\n" +
                           "                  </div>\n" +
                           "                    </div>\n" +
                           "                  </form>\n"+
                           "                </div>\n" +
                           "              </div>\n" +
                           "            </div>\n" +
                    // Daqui pra baixo são os dados da matrícula da pessoa
                           "            <div class='col-md-5'>\n" +
                           "              <div class='card'>\n" +
                           "                <div class='card-header card-header-primary'>\n" +
                           "                  <h4 class='card-title'>Matrícula do aluno</h4>\n" +
                           "                  <p class='card-category'>As informações da matrícula deste aluno</p>\n" +
                           "                </div>\n" +
                           "                <div class='card-body'>" +
                           "                  <form action='SelecionaTurmas' method='get'>\n" +
                           "                    <div class='row'>\n" +
                           "                      <div class=\"table-responsive\">\n" +
                           "                    <table class=\"table\">\n" +
                           "                      <thead class=\" text-primary\">\n" +
                           "                        <th>\n" +
                           "                          Matrícula\n" +
                           "                        </th>\n" +
                           "                        <th>\n" +
                           "                          Nome\n" +
                           "                        </th>\n" +
                           "                        <th>\n" +
                           "                          Curso\n" +
                           "                        </th>\n" +
                           "                      </thead>\n" +
                           "                      <tbody>\n" +
                           "                        <tr>\n" +
                           "                           <td>"+ aluno_matricula +"</td>\n" +
                           "                           <td>"+ aluno_nome +"</td>\n" +
                           "                           <td>"+ aluno_curso +"</td>\n" +
                           "                        </tr>\n" +                           
                           "                      </tbody>\n" +
                           "                    </table>\n" +
                           "                  </div>\n" +
                           "                    </div>\n" +
                           "                  </form>\n"+
                           "                </div>\n" +
                           "              </div>\n" +
                           "            </div>\n" +
                           "          </div>\n";
                    
                    

               /* TODO output your page here. You may use following sample code. */
               out.println("<!DOCTYPE html>\n" +
                           "<html lang='pt-br'>\n" +
                           "<head>\n" +
                           "  <meta charset='utf-8' />\n" +
                           "  <link rel='apple-touch-icon' sizes='76x76' href='assets/img/apple-icon.png'>\n" +
                           "  <link rel='icon' type='image/png' href='assets/img/favicon.png'>\n" +
                           "  <meta http-equiv='X-UA-Compatible' content='IE=edge,chrome=1' />\n" +
                           "  <title>\n" +
                           "    ATIVIDADE LISTA N1\n" +
                           "  </title>\n" +
                           "  <meta content='width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=0, shrink-to-fit=no' name='viewport' />\n" +
                           "  <!--     Fonts and icons     -->\n" +
                           "  <link rel='stylesheet' type='text/css' href='https://fonts.googleapis.com/css?family=Roboto:300,400,500,700|Roboto+Slab:400,700|Material+Icons' />\n" +
                           "  <link rel='stylesheet' href='https://maxcdn.bootstrapcdn.com/font-awesome/latest/css/font-awesome.min.css'>\n" +
                           "  <!-- CSS Files -->\n" +
                           "  <link href='assets/css/material-dashboard.css?v=2.1.0' rel='stylesheet' />\n" +
                           "  <!-- CSS Just for demo purpose, don't include it in your project -->\n" +
                           "  <link href='assets/demo/demo.css' rel='stylesheet' />\n" +
                           "</head>\n" +
                           "\n" +
                           "<body class=''>\n" +
                           "  <div class='wrapper '>\n" +
                           "    <div class='sidebar' data-color='purple' data-background-color='white' data-image='assets/img/sidebar-1.jpg'>\n" +
                           "      <!--\n" +
                           "        Tip 1: You can change the color of the sidebar using: data-color='purple | azure | green | orange | danger'\n" +
                           "\n" +
                           "        Tip 2: you can also add an image using data-image tag\n" +
                           "    -->\n" +
                           "      <div class='logo simple-text logo-normal'>\n" +
                           "        <span class='simple-text logo-normal'>\n" +
                           "          PROGRAMAÇÃO WEB\n" +
                           "        </span>\n" +
                           "      </div>\n" +
                           "      <div class='sidebar-wrapper'>\n" +
                           "        <ul class='nav'>\n" +
                           "          <li class='nav-item active '>\n" +
                           "            <a class='nav-link' href='./tables.html'>\n" +
                           "              <i class='material-icons'>content_paste</i>\n" +
                           "              <p>Solicitação matrícula</p>\n" +
                           "            </a>\n" +
                           "          </li>\n" +
                           "        </ul>\n" +
                           "      </div>\n" +
                           "    </div>\n" +
                           "    <div class='main-panel'>\n" +
                           "      <!-- Navbar -->\n" +
                           "      <nav class='navbar navbar-expand-lg navbar-transparent navbar-absolute fixed-top '>\n" +
                           "        <div class='container-fluid'>\n" +
                           "          <div class='navbar-wrapper'>\n" +
                           "            <a class='navbar-brand' href='#'>Solicitação de Matrícula</a>\n" +
                           "          </div>\n" +
                           "          <button class='navbar-toggler' type='button' data-toggle='collapse' aria-controls='navigation-index' aria-expanded='false' aria-label='Toggle navigation'>\n" +
                           "            <span class='sr-only'>Toggle navigation</span>\n" +
                           "            <span class='navbar-toggler-icon icon-bar'></span>\n" +
                           "            <span class='navbar-toggler-icon icon-bar'></span>\n" +
                           "            <span class='navbar-toggler-icon icon-bar'></span>\n" +
                           "          </button>\n" +
                           "          <div class='collapse navbar-collapse justify-content-end'>\n" +
                           "            <form class='navbar-form'>\n" +
                           "              <div class='input-group no-border'>\n" +
                           "                <input type='text' value='' class='form-control' placeholder='Search...'>\n" +
                           "                <button type='submit' class='btn btn-white btn-round btn-just-icon'>\n" +
                           "                  <i class='material-icons'>search</i>\n" +
                           "                  <div class='ripple-container'></div>\n" +
                           "                </button>\n" +
                           "              </div>\n" +
                           "            </form>\n" +
                           "            <ul class='navbar-nav'>\n" +
                           "              <li class='nav-item'>\n" +
                           "                <a class='nav-link' href='#'>\n" +
                           "                  <i class='material-icons'>dashboard</i>\n" +
                           "                  <p class='d-lg-none d-md-block'>\n" +
                           "                    Stats\n" +
                           "                  </p>\n" +
                           "                </a>\n" +
                           "              </li>\n" +
                           "              <li class='nav-item dropdown'>\n" +
                           "                <a class='nav-link' href='#' id='navbarDropdownMenuLink' data-toggle='dropdown' aria-haspopup='true' aria-expanded='false'>\n" +
                           "                  <i class='material-icons'>notifications</i>\n" +
                           "                  <span class='notification'>0</span>\n" +
                           "                  <p class='d-lg-none d-md-block'>\n" +
                           "                    Some Actions\n" +
                           "                  </p>\n" +
                           "                </a>\n" +
                           "                <div class='dropdown-menu dropdown-menu-right' aria-labelledby='navbarDropdownMenuLink'>\n" +
                           "                  <a class='dropdown-item' href='#'>Sem notificações</a>\n" +
                           "                </div>\n" +
                           "              </li>\n" +
                           "              <li class='nav-item'>\n" +
                           "                <a class='nav-link' href='#'>\n" +
                           "                  <i class='material-icons'>person</i>\n" +
                           "                  <p class='d-lg-none d-md-block'>\n" +
                           "                    Account\n" +
                           "                  </p>\n" +
                           "                </a>\n" +
                           "              </li>\n" +
                           "            </ul>\n" +
                           "          </div>\n" +
                           "        </div>\n" +
                           "      </nav>\n" +
                           "      <!-- End Navbar -->\n" +
                           "      <div class='content'>\n" +
                           "        <div class='container-fluid'>\n");
               
               out.println(conteudo);                    

               out.println("        </div>\n" +
                           "      </div>\n" +
                           "      <footer class='footer'>\n" +
                           "        <div class='container-fluid'>\n" +
                           "          <nav class='float-left'>\n" +
                           "            <ul>\n" +
                           "              <li>\n" +
                           "                <a href='#'>\n" +
                           "                  Solicitação\n" +
                           "                </a>\n" +
                           "              </li>\n" +
                           "            </ul>\n" +
                           "          </nav>\n" +
                           "          <div class='copyright float-right'>\n" +
                           "            &copy;\n" +
                           "            <script>\n" +
                           "              document.write(new Date().getFullYear())\n" +
                           "            </script>, made with <i class='material-icons'>favorite</i> by\n" +
                           "            <a href='#' target='_blank'>Ariel Pierot</a> for a better web.\n" +
                           "          </div>\n" +
                           "        </div>\n" +
                           "      </footer>\n" +
                           "    </div>\n" +
                           "  </div>\n" +
                           "  <!--   Core JS Files   -->\n" +
                           "  <script src='assets/js/core/jquery.min.js' type='text/javascript'></script>\n" +
                           "  <script src='assets/js/core/popper.min.js' type='text/javascript'></script>\n" +
                           "  <script src='assets/js/core/bootstrap-material-design.min.js' type='text/javascript'></script>\n" +
                           "  <script src='assets/js/plugins/perfect-scrollbar.jquery.min.js'></script>\n" +
                           "  <!-- Chartist JS -->\n" +
                           "  <script src='assets/js/plugins/chartist.min.js'></script>\n" +
                           "  <!--  Notifications Plugin    -->\n" +
                           "  <script src='assets/js/plugins/bootstrap-notify.js'></script>\n" +
                           "  <!-- Control Center for Material Dashboard: parallax effects, scripts for the example pages etc -->\n" +
                           "  <script src='assets/js/material-dashboard.min.js?v=2.1.0' type='text/javascript'></script>\n" +
                           "  <!-- Material Dashboard DEMO methods, don't include it in your project! -->\n" +
                           "  <script src='assets/demo/demo.js'></script>\n" +
                           "</body>\n" +
                           "\n" +
                           "</html>\n");
               out.close();
        }
        catch (SQLException | ClassNotFoundException sqlException)                                
        {                                                                  
           sqlException.printStackTrace();
           return;                                               
        }
          // fim do catch
           // fim do catch
        finally // assegura que a instruÃ§Ã£o e conexÃ£o sÃ£o fechadas adequadamente
        {                                                             
           try                                                       
           {                                                          
              statement.close();                                      
              connection.close();                                     
           } // fim do try
           catch ( Exception exception )                              
           {                                                          
              exception.printStackTrace();                            
              return;                                       
           } // fim do catch
        } // fim do finally 
    }
    
    public String data_formatada (String horario)
    {
        String data_formatada = "";
        
        int dia = 0;
        int horario_inicial = 0;
        int horario_final = 0;
        String turno = "";
        dia = Integer.parseInt(horario.substring(0, 1));

        switch (dia) {
            case 2:  data_formatada += "Seg ";
                     break;
            case 3:  data_formatada += "Ter ";
                     break;
            case 4:  data_formatada += "Qua ";
                     break;
            case 5:  data_formatada += "Qui ";
                     break;
            case 6:  data_formatada += "Sex ";
                     break;
        }

        turno = horario.substring(1, 2);
        horario_inicial = Integer.parseInt(horario.substring(2, 3));
        horario_final = Integer.parseInt(horario.substring(horario.length()-1, horario.length()));

        if(turno.equals("M"))
        {
            switch (horario_inicial) {
                case 1:  data_formatada += "07-";
                         break;
                case 2:  data_formatada += "08-";
                         break;
                case 3:  data_formatada += "09-";
                         break;
                case 4:  data_formatada += "10-";
                         break;
                case 5:  data_formatada += "11-";
                         break;
                case 6:  data_formatada += "13-";
                         break;
            }
            
            switch (horario_final) {
                case 2:  data_formatada += "09";
                         break;
                case 3:  data_formatada += "10";
                         break;
                case 4:  data_formatada += "11";
                         break;
                case 5:  data_formatada += "12";
                         break;
                case 6:  data_formatada += "13";
                         break;
            }
        }
        
        if(turno.equals("T"))
        {
            switch (horario_inicial) {
                case 1:  data_formatada += "13-";
                         break;
                case 2:  data_formatada += "14-";
                         break;
                case 3:  data_formatada += "15-";
                         break;
                case 4:  data_formatada += "16-";
                         break;
                case 5:  data_formatada += "17-";
                         break;
                case 6:  data_formatada += "18-";
                         break;
            }
            
            switch (horario_final) {
                case 2:  data_formatada += "15";
                         break;
                case 3:  data_formatada += "16";
                         break;
                case 4:  data_formatada += "17";
                         break;
                case 5:  data_formatada += "18";
                         break;
                case 6:  data_formatada += "19";
                         break;
            }
        }
        
        if(turno.equals("N"))
        {
            switch (horario_inicial) {
                case 1:  data_formatada += "19-";
                         break;
                case 2:  data_formatada += "20-";
                         break;
                case 3:  data_formatada += "21-";
                         break;
                case 4:  data_formatada += "22-";
                         break;
            }
            
            switch (horario_final) {
                case 2:  data_formatada += "21";
                         break;
                case 3:  data_formatada += "22";
                         break;
                case 4:  data_formatada += "23";
                         break;
            }
        }
        
        return data_formatada;
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }
    

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}
