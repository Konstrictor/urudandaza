package bi.konstrictor.urudandaza.models;

import android.content.Context;
import android.widget.Toast;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.misc.TransactionManager;
import com.j256.ormlite.table.DatabaseTable;

import java.sql.SQLException;
import java.util.Date;
import java.util.concurrent.Callable;

import bi.konstrictor.urudandaza.Globals;
import bi.konstrictor.urudandaza.InkoranyaMakuru;
import bi.konstrictor.urudandaza.interfaces.Model;

@DatabaseTable
public class RemboursementVente implements Model {
    @DatabaseField(generatedId = true)
    public Integer id;
    @DatabaseField(canBeNull=false, foreign=true, foreignColumnName="id", foreignAutoCreate=true)
    public Vente vente;
    @DatabaseField
    public Double payee;
    @DatabaseField
    public String motif;
    @DatabaseField
    public Date date;
    @DatabaseField
    public String checksum;
    @DatabaseField(canBeNull=false, foreign=true, foreignColumnName="id")
    private Password signature;

    public RemboursementVente() {
    }

    public RemboursementVente(Vente vente, Double payee, String motif) {
        this.vente = vente;
        this.payee = payee;
        this.motif = motif;
        this.date = new Date();
    }

    public void validate(Password password){
        this.signature = password;
        this.checksum = Globals.sign(""+payee+date.getTime(), password.getSignature());
    }

    public boolean is_valid(){
        return checksum.equals(Globals.sign(""+payee+date.getTime(), this.signature.getSignature()));
    }

    @Override
    public void create(final Context context) {
        InkoranyaMakuru inkoranyaMakuru = new InkoranyaMakuru(context);
        try {
            final Dao<RemboursementVente, Integer> dao = inkoranyaMakuru.getDao(RemboursementVente.class);
            final Dao<Vente, Integer> daoAS = inkoranyaMakuru.getDao(Vente.class);
            final Dao<Cloture, Integer> daoCloture = inkoranyaMakuru.getDao(Cloture.class);
            final Cloture cloture = vente.cloture;
            cloture.payee_vente += payee;
            vente.payee += payee;
            try {
                TransactionManager.callInTransaction(inkoranyaMakuru.getConnectionSource(),
                    new Callable<Void>(){
                        @Override
                        public Void call() throws Exception {
                            daoAS.update(vente);
                            daoCloture.update(cloture);
                            dao.create(RemboursementVente.this);
                            Toast.makeText(context, "Vyagenze neza", Toast.LENGTH_LONG).show();
                            return null;
                        }
                    });
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            Toast.makeText(context, "ntivyakunze", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    @Override
    public void update(Context context) {
        InkoranyaMakuru inkoranyaMakuru = new InkoranyaMakuru(context);
        try {
            final Dao<RemboursementVente, Integer> dao = inkoranyaMakuru.getDao(RemboursementVente.class);
            dao.update(this);
            Toast.makeText(context, "Vyagenze neza", Toast.LENGTH_LONG).show();
        } catch (SQLException e) {
            Toast.makeText(context, "ntivyakunze", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    @Override
    public void delete(Context context) {
        InkoranyaMakuru inkoranyaMakuru = new InkoranyaMakuru(context);
        try {
            final Dao<RemboursementVente, Integer> dao = inkoranyaMakuru.getDao(RemboursementVente.class);
            dao.delete(this);
            Toast.makeText(context, "Vyagenze neza", Toast.LENGTH_LONG).show();
        } catch (SQLException e) {
            Toast.makeText(context, "ntivyakunze", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }
}
