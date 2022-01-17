import java.sql.*;

public class queries {
    private static final String URL = "jdbc:postgresql://10.62.73.22:5432/";
    private static final String username = "l3d43";
    private static final String password = "l3d43";
    private static Connection con = null;
    private static Statement stmt = null;
    private static PreparedStatement pstmt = null;
    private static ResultSet rs = null;

    private static final int SIZE_NUMBER_PART_ID = 4;

    public static void test() throws SQLException {
        //query2d();
        //query2e();
        //query3c();
        //query3d();
        //System.out.println(checkEquipasMin2Elements());
        //System.out.println(getEquipaFromId(1));
        //substituirElem(1,3);
        //novoActivo("test",true,"2020-02-03",null,null,"Lisboa","Z0005",1,1,1);
        //activoForaServico("a0001");
        custoTotalActivo("z0002");
    }

    // partimos do pressuposto que o id nunca irá ultrapassar a parte do numero
    private static String increaseId(String id){
        String nextId = id;
        char firstChar = nextId.charAt(0);
        nextId = nextId.substring(1);
        int numPart = Integer.parseInt(nextId);
        nextId = firstChar + fillZeros(numPart+1);
        return nextId;
    }

    private static String fillZeros(int number){
        String numb = String.valueOf(number);
        String newId = "";
        if(numb.length()> SIZE_NUMBER_PART_ID) return "";
        for(int n = numb.length(); n< SIZE_NUMBER_PART_ID; n++){
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

    public static boolean checkEquipasMin2Elements() throws SQLException {
        //true if DB follows restriction
        boolean conditionCheck = false;
        try {
            connect();
            stmt = con.createStatement();
            rs = stmt.executeQuery("SELECT equipa, count(equipa) FROM PESSOA group by equipa order by equipa");
            while (rs.next()) {
                if(rs.getInt(2)>=2) {conditionCheck = true;
                }else {
                    conditionCheck = false;
                    break;
                }
            }
        } catch (SQLException e) { e.printStackTrace();
        } finally {closeConnection(); }
        return conditionCheck;
    }

    public static boolean checkEquipasMinXElements(int equipa, int numElem) throws SQLException {
        //true if DB follows restriction
        boolean conditionCheck = false;
        try {
            connect();
            pstmt = con.prepareStatement("SELECT count(equipa) FROM PESSOA where equipa = ? group by equipa order by equipa");
            pstmt.setInt(1, equipa);
            rs = pstmt.executeQuery();
            if(rs.next() && rs.getInt(1)>=numElem) conditionCheck = true;
        } catch (SQLException e) { e.printStackTrace();
        } finally {closeConnection(); }
        return conditionCheck;
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

    //passar estado sempre como activo? estado=1
    //set String ou set Date
    public static void novoActivo(String nome, Boolean estado, String dt, String marca, String modelo, String local, String idactivotp, int tipo, int empresa, int pessoa) throws SQLException {
        String newId = increaseId(stQueryResString("SELECT MAX(id) FROM ACTIVO"));
        try {
            connect();
            pstmt = con.prepareStatement("INSERT INTO ACTIVO (id, nome, estado, dtaquisicao, marca, modelo, localizacao, idactivotopo, tipo, empresa, pessoa) " +
                    "VALUES (?,?,?::bit,?,?,?,?,?,?,?,?)");
            //INSERT INTO ACTIVO(id, nome, estado, dtaquisicao, marca, modelo, localizacao, idactivotopo, tipo, empresa, pessoa)VALUES ('a0001','cena1','1','2021-02-02',NULL,NULL,'ali','a0001',3,1,2)
            pstmt.setString(1, newId);
            pstmt.setString(2,nome);
            //como passar bit?
            //estado
            var estadoBit = 0;
            if(estado) estadoBit = 1;
            pstmt.setInt(3,estadoBit);
            /*org.postgresql.util.PSQLException: ERROR: cannot cast type boolean to bit
            pstmt.setBoolean(3,estado);*/
            pstmt.setDate(4, Date.valueOf(dt));
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

    //replace é colocar lo para uma outra equipa ou eliminar lo da tabela?
    public static void substituirElem(int idToReplaceOut, int idToReplaceIn) throws SQLException {
        int equipa = getEquipaFromId(idToReplaceOut);
        if(!checkEquipasMinXElements(getEquipaFromId(idToReplaceIn),3)){
            System.out.println("Can't replace elements");
            System.out.println("Team from element that would replace, would have less than 2 elements");
            return;
        }
        //updateEquipaElem(idToReplaceOut,1);
        pstUpdate("update PESSOA set equipa = null where id = ?",idToReplaceOut);
        updateEquipaElem(idToReplaceIn,equipa);
    }

    private static int getEquipaFromId(int id) throws SQLException {
        int equipa = -1;
        try {
            connect();
            pstmt = con.prepareStatement("SELECT equipa FROM PESSOA WHERE id=?");
            pstmt.setInt(1, id);    //ident reserva
            rs = pstmt.executeQuery();
            if(rs.next()) equipa = rs.getInt(1);
        } catch (SQLException e) {e.printStackTrace();
        } finally { closeConnection();}
        return equipa;
    }

    private static void updateEquipaElem(int id, int equipa) throws SQLException {
        try {
            connect();
            pstmt = con.prepareStatement("update PESSOA set equipa = ? where id = ?");
            pstmt.setInt(1, equipa);
            pstmt.setInt(2, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {e.printStackTrace();
        } finally { closeConnection();}
    }

    //activo fora de serviço == estado=0?
    //adicionar restrição ACTIVO estado=0 n sofre intervenções
    //limpar intervenções?
    public static void activoForaServico(String idActivo) throws SQLException {
        pstUpdate("update ACTIVO set estado = '0' where id = ?",idActivo);
    }

    //partimos do pressuposto que o activo tem um valor comercial estipulado na data de aquisição
    public static void custoTotalActivo(String id) throws SQLException {
        System.out.println(pstQuerry("select valor from (ACTIVO inner join VCOMERCIAL on id = VCOMERCIAL.activo) where dtvcomercial = dtaquisicao and id = '?'", id));
        System.out.println(stQueryResInt("select valor from (ACTIVO inner join VCOMERCIAL on id = VCOMERCIAL.activo)" +
                " where dtvcomercial = dtaquisicao and id = 'z0002'"));
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

    private static String stQueryResString(String query) throws SQLException {
        String res = "";
        try {
            connect();
            stmt = con.createStatement();
            rs = stmt.executeQuery(query);
            if (rs.next()) res = rs.getString(1);
        } catch (SQLException e) {e.printStackTrace();
        } finally { closeConnection();}
        return res;
    }

    private static int stQueryResInt(String query) throws SQLException {
        int res = -1;
        try {
            connect();
            stmt = con.createStatement();
            rs = stmt.executeQuery(query);
            if (rs.next()) res = rs.getInt(1);
        } catch (SQLException e) {e.printStackTrace();
        } finally { closeConnection();}
        return res;
    }

    private static int pstQuerry(String query, String val) throws SQLException {
        int res = -1;
        try {
            connect();
            pstmt = con.prepareStatement(query);
            pstmt.setString(1, val);
            pstmt.executeQuery();
            if(rs.next()) res = rs.getInt(1);
        } catch (SQLException e) {e.printStackTrace();
        } finally { closeConnection();}
        return res;
    }

    private static void pstQuerry(String query, int val) throws SQLException {
        try {
            connect();
            pstmt = con.prepareStatement(query);
            pstmt.setInt(1, val);
            pstmt.executeQuery();
        } catch (SQLException e) {e.printStackTrace();
        } finally { closeConnection();}
    }

    private static void pstUpdate(String query, String val) throws SQLException {
        try {
            connect();
            pstmt = con.prepareStatement(query);
            pstmt.setString(1, val);
            pstmt.executeUpdate();
        } catch (SQLException e) {e.printStackTrace();
        } finally { closeConnection();}
    }

    private static void pstUpdate(String query, int val) throws SQLException {
        try {
            connect();
            pstmt = con.prepareStatement(query);
            pstmt.setInt(1, val);
            pstmt.executeUpdate();
        } catch (SQLException e) {e.printStackTrace();
        } finally { closeConnection();}
    }
}
