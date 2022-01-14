import java.sql.*;

public class queries {
    private static final String URL = "jdbc:postgresql://10.62.73.22:5432/";
    private static final String username = "l3d43";
    private static final String password = "l3d43";
    private static Connection con = null;
    private static Statement stmt = null;
    private static PreparedStatement pstmt = null;
    private static ResultSet rs = null;

    private static int SIZE_NUMBER_PART_ID = 4;

    public static void test() throws SQLException {
        /*connect();
        String maxId = queries.maxIdActivo();
        closeConnection();
        System.out.println(maxId);

        String newId = increaseId(maxId);
        System.out.println(newId);*/

        //queries done with fixed values only need to implement interchangeable vars and prepared statements
        /*query2d();
        query2e();
        query3c();
        query3d();*/
        checkEquipasMin2Elements();
    }

    // partimos do pressuposto que o id nunca irá ultrapassar a parte do numero
    private static String increaseId(String id){
        String nextId = id;
        char firstChar = nextId.charAt(0);
        nextId = nextId.substring(1);
        int numPart = Integer.parseInt(nextId);
        nextId = firstChar + fillZeros(numPart+1,SIZE_NUMBER_PART_ID);
        return nextId;
    }

    private static String fillZeros(int number, int numberOfDigits){
        String numb = String.valueOf(number);
        String newId = "";
        if(numb.length()>numberOfDigits) return "";
        for(int n=numb.length(); n<numberOfDigits; n++){
            newId += "0";
        }return newId += String.valueOf(number);
    }

    private static void connect() throws SQLException {
        try {
            con = DriverManager.getConnection(URL, username, password);
        } catch (SQLException sqlex) {
            System.out.println("Erro : " + sqlex.getMessage());
        }
    }

    private static void closeConnection() throws SQLException {
        try {
            // free the resources of the ResultSet
            if (rs != null) rs.close();
            // free the resources of the Statement
            if (stmt != null) stmt.close();
            // close connection
            if (con != null) con.close();
        } catch (Exception e) {e.printStackTrace();}
    }

    public static void exampleTest() throws SQLException {
        try {
            connect();
            stmt = con.createStatement();
            rs = stmt.executeQuery("select nome, modelo, marca, localizacao from ACTIVO where estado = '1';");
            App.printTable(rs,4);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeConnection();
        }
    }

    public static void checkEquipasMin2Elements() throws SQLException {
        try {
            connect();
            stmt = con.createStatement();
            rs = stmt.executeQuery("SELECT equipa FROM PESSOA order by equipa");
            //prints true if DB follows restriction
            System.out.println(min2Elements(rs));
        } catch (SQLException e) { e.printStackTrace();
        } finally {closeConnection(); }

    }

    private static boolean min2Elements(ResultSet rs) throws SQLException {
        int currentTeam = -1;
        int lastTeam = -1;
        boolean min2Elements = false;
        while (rs.next()) {
            currentTeam = rs.getInt(1);
            if(lastTeam != -1 && currentTeam == lastTeam){
                // all teams have 2 elements
                min2Elements = true;
            }
            if(lastTeam != -1 && currentTeam != lastTeam && !min2Elements){
                // atleast 1 does not have min 2 elements
                return false;
            }
            lastTeam=currentTeam;
        }
        return true;
    }

    public static void checkRestrictionActivohierarchy() {

    }

    public static void checkRestrictionIntervencaoDtFim() {

    }

    public static void checkRestrictionIntervencao() {

    }

    public static void checkRestrictionVcomercial() {

    }



    public static void checkRestrictionConflictGestaoMan() {

    }

    //colocar marca e modelo a null se tal n for especificado ou ter 2 metodos, um com marca e modelo e outro sem
    //set Strig ou set Date
    public static void novoActivo(String nome, Boolean estado, String dt, String marca, String modelo, String local, String idactivotp, int tipo, int empresa, int pessoa) throws SQLException {
        try {
            connect();
            stmt = con.createStatement();
            pstmt = con.prepareStatement("INSERT INTO ACTIVO (id, nome, estado, dtaquisicao, marca, modelo, localizacao, idactivotopo, tipo, empresa, pessoa) " +
                    "VALUES (?,?,?,?,?,?,?,?,?,?,?)");
            //INSERT INTO ACTIVO(id, nome, estado, dtaquisicao, marca, modelo, localizacao, idactivotopo, tipo, empresa, pessoa)VALUES ('a0001','cena1','1','2021-02-02',NULL,NULL,'ali','a0001',3,1,2)
            pstmt.setString(1, increaseId(maxIdActivo()));    //ident reserva
            pstmt.setString(2,nome);
            //como passar bit?
            char temp = '0';
            if(estado) temp = '1';
            String tempString = "" + temp;
            //estado
            pstmt.setString(3,tempString);
            //pstmt.setDate(4,dt);
            pstmt.setString(4,dt);
            pstmt.setString(5,marca);
            pstmt.setString(6,modelo);
            pstmt.setString(7,local);
            pstmt.setString(8,idactivotp);
            pstmt.setInt(9,tipo);
            pstmt.setInt(10,empresa);
            pstmt.setInt(11,pessoa);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeConnection();
        }
    }

    // FECHO E VOLTO A LIGAR A CON/LIMPAR RECURSOS
    private static String maxIdActivo() throws SQLException {
        stmt = con.createStatement();
        rs = stmt.executeQuery("SELECT MAX(id) FROM ACTIVO");
        rs.next();
        return rs.getString(1);
    }

    public static void substituirElem() {

    }

    public static void activoForaServico() {

    }

    public static void custoTotalActivo() {

    }

    public static void query2d() throws SQLException {
        try {
            connect();
            stmt = con.createStatement();
            rs = stmt.executeQuery("select ACTIVO.nome, PESSOA.equipa, PESSOA.nome from " +
                    "(PESSOA full outer join INTER_EQUIPA " +
                    "on PESSOA.equipa=INTER_EQUIPA.equipa full outer join INTERVENCAO " +
                    "on INTER_EQUIPA.intervencao=INTERVENCAO.noint left outer join ACTIVO " +
                    "on INTERVENCAO.activo=ACTIVO.id) where ((INTERVENCAO.estado " +
                    "IN ('em execução','em análise')) and ACTIVO.nome = 'válvula de ar condicionado') " +
                    "union select ACTIVO.nome, PESSOA.equipa, PESSOA.nome from (PESSOA full outer join ACTIVO " +
                    "on PESSOA.id=ACTIVO.pessoa) where ACTIVO.nome = 'válvula de ar condicionado';");
            /*rs = pstmt.executeQuery();
            rs.next();
            val = rs.getString("ACTIVO.nom");*/
            System.out.println("query2d");
            App.printTable(rs,3);
        } catch (SQLException e) {e.printStackTrace();}
        finally {closeConnection();}
    }

    public static void query2e() throws SQLException {
        try {
            connect();
            stmt = con.createStatement();
            rs = stmt.executeQuery("select A.nome from (ACTIVO as A inner join PESSOA as P on pessoa = P.id) " +
                    "where P.nome = 'Manuel Fernandes'" +
                    "union " +
                    "select ACTIVO.nome as nomeInterv from (ACTIVO inner join INTERVENCAO " +
                    "on ACTIVO.id = INTERVENCAO.activo inner join " +
                    "INTER_EQUIPA on INTERVENCAO.noint = INTER_EQUIPA.intervencao inner join PESSOA " +
                    "on INTER_EQUIPA.equipa = PESSOA.equipa) where PESSOA.nome ='Manuel Fernandes';");
            System.out.println("query2e");
            App.printTable(rs,1);
        } catch (SQLException e) {e.printStackTrace();}
        finally {closeConnection();}
    }

    public static void query3c() throws SQLException {
        try {
            connect();
            stmt = con.createStatement();
            rs = stmt.executeQuery("select DISTINCT PESSOA.nome, profissao, telefone from (PESSOA " +
                    "left outer join TEL_PESSOA on PESSOA.id=TEL_PESSOA.pessoa inner join ACTIVO " +
                    "on PESSOA.id=ACTIVO.pessoa);");
            System.out.println("query3c");
            App.printTable(rs,3);
        } catch (SQLException e) {e.printStackTrace();}
        finally {closeConnection();}
    }

    public static void query3d() throws SQLException {
        try {
            connect();
            stmt = con.createStatement();
            rs = stmt.executeQuery("select noint from (INTERVENCAO join INTER_EQUIPA " +
                    "on INTERVENCAO.noint=INTER_EQUIPA.intervencao) where dtinicio between " +
                    "date_trunc('month', CURRENT_DATE + interval '1 month' ) and date_trunc('month', " +
                    "CURRENT_DATE + interval '2 month') ;");
            System.out.println("query3d");
            App.printTable(rs,1);
        } catch (SQLException e) {e.printStackTrace();}
        finally {closeConnection();}
    }



}
