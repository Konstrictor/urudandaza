package bi.konstrictor.urudandaza.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.text.Editable;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.util.List;

import bi.konstrictor.urudandaza.InkoranyaMakuru;
import bi.konstrictor.urudandaza.interfaces.OnTextChangeListener;
import bi.konstrictor.urudandaza.R;
import bi.konstrictor.urudandaza.interfaces.RefreshableActivity;
import bi.konstrictor.urudandaza.models.ActionStock;
import bi.konstrictor.urudandaza.models.Personne;
import bi.konstrictor.urudandaza.models.Produit;

public class KudandazaForm extends Dialog {
    private RefreshableActivity context;
    private TextView lbl_kudandaza_product, field_kudandaza_prix, field_kudandaza_total,
            field_kudandaza_qtt, field_kudandaza_payee, lbl_kudandaza_unite;
    private CheckBox check_kudandaza_expired;
    private AutoCompleteTextView field_kudandaza_personne;
    private String kudandaza_prix, kudandaza_qtt, client, kudandaza_payee;
    private ProgressBar progress_kudandaza;
    private String[] arrcontact;
    private Double payee, a_payer;
    private Produit produit;
    private boolean edition = false;
    private boolean ideni = false;
    private ActionStock action_stock;
    private boolean expired;

    public KudandazaForm(final RefreshableActivity context, Produit produit) {
        super(context, R.style.Theme_AppCompat_DayNight_Dialog);
        setContentView(R.layout.form_kudandaza);
        this.context = context;
        this.produit = produit;

        lbl_kudandaza_product = findViewById(R.id.lbl_kudandaza_product);
        field_kudandaza_qtt = findViewById(R.id.field_kudandaza_qtt);
        field_kudandaza_prix = findViewById(R.id.field_kudandaza_prix);
        field_kudandaza_total = findViewById(R.id.field_kudandaza_total);
        field_kudandaza_payee = findViewById(R.id.field_kudandaza_payee);
        field_kudandaza_personne = findViewById(R.id.field_kudandaza_personne);
        check_kudandaza_expired = findViewById(R.id.check_kudandaza_expired);
        lbl_kudandaza_unite = findViewById(R.id.lbl_kudandaza_unite);
        progress_kudandaza = findViewById(R.id.progress_kudandaza);

        Button btn_kudandaza_submit = findViewById(R.id.btn_kudandaza_submit);
        Button btn_kudandaza_cancel = findViewById(R.id.btn_kudandaza_cancel);
        Button btn_reset_payee = findViewById(R.id.btn_reset_payee);

        lbl_kudandaza_product.setText(produit.nom);
        lbl_kudandaza_unite.setText(produit.unite_entrant);
        btn_kudandaza_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        btn_kudandaza_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submit();
            }
        });
        btn_reset_payee.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { field_kudandaza_payee.setText("0"); }
        });
        init();
    }
    private void init() {
        field_kudandaza_qtt.addTextChangedListener(new OnTextChangeListener() {
            @Override
            public void textChanged(Editable s) {
                if(field_kudandaza_qtt.hasFocus()) {
                    String str_prix = field_kudandaza_prix.getText().toString().trim();
                    String str_total = field_kudandaza_total.getText().toString().trim();
                    if (s.length()<1) return;
                    Double quantity = Double.parseDouble(s.toString());
                    if (!str_prix.isEmpty()) {
                        Double prix = Double.parseDouble(str_prix);
                        Double total;
                        total = prix*quantity;
                        field_kudandaza_total.setText(total.toString());
                        field_kudandaza_payee.setText(total.toString());
                    } else if (!str_total.isEmpty()) {
                        Double total = Double.parseDouble(str_total);
                        Double prix;
                        prix = total/quantity;
                        field_kudandaza_prix.setText(prix.toString());
                        field_kudandaza_payee.setText(total.toString());
                    }
                }
            }
        });
        field_kudandaza_total.addTextChangedListener(new OnTextChangeListener() {
            @Override
            public void textChanged(Editable s) {
                if(field_kudandaza_total.hasFocus()) {
                    String str_qtt = field_kudandaza_qtt.getText().toString().trim();
                    if ((!str_qtt.isEmpty()) && s.length()>0) {
                        Double quantity = Double.parseDouble(str_qtt);
                        Double total = Double.parseDouble(s.toString());
                        Double prix = total/quantity;
                        field_kudandaza_prix.setText(prix.toString());
                        field_kudandaza_payee.setText(total.toString());
                    }
                }
            }
        });

        field_kudandaza_prix.addTextChangedListener(new OnTextChangeListener() {
            @Override
            public void textChanged(Editable s) {
                if (field_kudandaza_prix.hasFocus()) {
                    String str_qtt = field_kudandaza_qtt.getText().toString().trim();
                    if ((!str_qtt.isEmpty()) && s.length()>0) {
                        Double quantity = Double.parseDouble(str_qtt);
                        Double prix = Double.parseDouble(s.toString());
                        Double total = prix * quantity;
                        field_kudandaza_total.setText(total.toString());
                        field_kudandaza_payee.setText(total.toString());
                    }
                }
            }
        });
        field_kudandaza_personne.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    progress_kudandaza.setVisibility(View.VISIBLE);
                    loadClient();
                    field_kudandaza_personne.setAdapter(new ArrayAdapter<String>(context,
                            android.R.layout.simple_dropdown_item_1line, arrcontact));
                    progress_kudandaza.setVisibility(View.GONE);
                }
            }
        });
        check_kudandaza_expired.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (check_kudandaza_expired.isChecked()){
                    new AlertDialog.Builder(context)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setTitle("Ivyononekanye")
                            .setMessage("uremeje vy'ukuri ko ivyo ari ivyononekaye?")
                            .setPositiveButton("Ego", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    setExpired(true);
                                }
                            })
                            .setNegativeButton("Oya", null)
                            .show();
                }else{
                    setExpired(false);
                }
            }
        });
    }
    public void setExpired(boolean expired) {
        this.expired = expired;
        if (expired){
            field_kudandaza_payee.setText("0");
            field_kudandaza_personne.setText("");
            field_kudandaza_total.setText("0");
            field_kudandaza_payee.setEnabled(false);
            field_kudandaza_personne.setEnabled(false);
            field_kudandaza_total.setEnabled(false);
        }else{
            field_kudandaza_payee.setEnabled(true);
            field_kudandaza_personne.setEnabled(true);
            field_kudandaza_total.setEnabled(true);
        }
    }
    private void loadClient() {
        try {
            Dao dao_clients = new InkoranyaMakuru(context).getDao(Personne.class);
            List<Personne> personnes = dao_clients.queryForAll();
            arrcontact = new String[personnes.size()];
            for (int i=0; i<personnes.size(); i++){
                arrcontact[i] = personnes.get(i).nom;
            }
        } catch (SQLException e) {
            Toast.makeText(context, "Erreur de lecture des contacts",Toast.LENGTH_LONG).show();
        }
    }
    public void build(){ show(); }
    public void chargerPersonne(){
        Personne personne = Personne.getClient(client, context);
        if (personne==null){
            ClientForm form = new ClientForm(context);
            form.setPARENT_NAME_FIELD(field_kudandaza_personne);
            form.show();
            progress_kudandaza.setVisibility(View.GONE);
        }
    }
    public void submit(){
        progress_kudandaza.setVisibility(View.VISIBLE);
        if (expired){
            performExpiration();
        }else{
            performSubmition();
        }
    }
    private void performExpiration() {
        getValues();
        double qtt = Double.parseDouble(kudandaza_qtt);
        double prix = Double.parseDouble(kudandaza_prix);
        action_stock.produit.prix = prix;
        action_stock.expiration(action_stock.produit, qtt, action_stock.cloture);
        action_stock.update(context);
        context.refresh();
        progress_kudandaza.setVisibility(View.GONE);
        dismiss();
    }
    private void performSubmition() {
        if(validateFields()) {
            Personne personne = null;
            InkoranyaMakuru inkoranyaMakuru = new InkoranyaMakuru(context);
            progress_kudandaza.setVisibility(View.VISIBLE);
            double qtt = Double.parseDouble(kudandaza_qtt);
            double prix = Double.parseDouble(kudandaza_prix);
            produit.prix = prix;
            if(ideni) {
                personne = Personne.getClient(client, context);
                if (personne == null) {
                    chargerPersonne();
                    return;
                }
            }
            if (edition) {
                action_stock.kudandaza(produit, qtt, personne, payee, null);
                action_stock.update(context);
            } else {
                ActionStock as = new ActionStock();
                as.kudandaza(produit, qtt, personne, payee, inkoranyaMakuru.getLatestCloture());
                as.create(context);
            }
            dismiss();
            context.refresh();
            Toast.makeText(context, "Vyagenze neza", Toast.LENGTH_LONG).show();
        }
        progress_kudandaza.setVisibility(View.GONE);
    }
    private void getValues(){
        kudandaza_qtt = field_kudandaza_qtt.getText().toString().trim();
        kudandaza_prix = field_kudandaza_prix.getText().toString().trim();
        kudandaza_payee = field_kudandaza_payee.getText().toString().trim();
        client = field_kudandaza_personne.getText().toString().trim();
    }
    private Boolean validateFields() {
        getValues();
        if(kudandaza_prix.isEmpty()){
            field_kudandaza_prix.setError("uzuza ngaha");
            return false;
        }
        if(kudandaza_qtt.isEmpty()){
            field_kudandaza_qtt.setError("uzuza ngaha");
            return false;
        }
        if(kudandaza_payee.isEmpty()){
            field_kudandaza_payee.setError("uzuza ngaha");
            return false;
        }
        a_payer = Double.parseDouble(kudandaza_prix)*Double.parseDouble(kudandaza_qtt);
        payee = Double.parseDouble(kudandaza_payee);
        if(payee<a_payer){
            ideni = true;
            if(client.isEmpty()) {
                field_kudandaza_personne.setError("ko atarishe yose uzuza izina");
                return false;
            }
        }
        return true;
    }

    public void setEdition(ActionStock as) {
        this.edition = true;
        this.action_stock = as;
        expired = as.perimee;
        Double qtt = Math.abs(as.getQuantite());
        lbl_kudandaza_product.setText(as.produit.nom);
        field_kudandaza_qtt.setText(qtt.toString());
        field_kudandaza_prix.setText(as.getPrix().toString());
        field_kudandaza_total.setText(as.getVenteTotal().toString());
        check_kudandaza_expired.setChecked(as.perimee);
        field_kudandaza_payee.setText(as.payee.toString());
        try {
            field_kudandaza_personne.setText(as.personne.nom);
        } catch (NullPointerException e){ }
    }
}
