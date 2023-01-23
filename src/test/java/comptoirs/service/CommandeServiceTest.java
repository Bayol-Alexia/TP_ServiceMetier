package comptoirs.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.time.LocalDate;

@SpringBootTest
 // Ce test est basé sur le jeu de données dans "test_data.sql"
class CommandeServiceTest {
    private static final String ID_PETIT_CLIENT = "0COM";
    private static final String ID_GROS_CLIENT = "2COM";
    private static final String VILLE_PETIT_CLIENT = "Berlin";
    private static final BigDecimal REMISE_POUR_GROS_CLIENT = new BigDecimal("0.15");


    @Autowired
    private CommandeService service;

    @Test
    void testCreerCommandePourGrosClient() {
        var commande = service.creerCommande(ID_GROS_CLIENT);
        assertNotNull(commande.getNumero(), "On doit avoir la clé de la commande");
        assertEquals(REMISE_POUR_GROS_CLIENT, commande.getRemise(),
            "Une remise de 15% doit être appliquée pour les gros clients");
    }

    @Test
    void testCreerCommandePourPetitClient() {
        var commande = service.creerCommande(ID_PETIT_CLIENT);
        assertNotNull(commande.getNumero());
        assertEquals(BigDecimal.ZERO, commande.getRemise(),
            "Aucune remise ne doit être appliquée pour les petits clients");
    }

    @Test
    void testCreerCommandeInitialiseAdresseLivraison() {
        var commande = service.creerCommande(ID_PETIT_CLIENT);
        assertEquals(VILLE_PETIT_CLIENT, commande.getAdresseLivraison().getVille(),
            "On doit recopier l'adresse du client dans l'adresse de livraison");
    }




    //On retrouve les mêmes tests pour les petits et gros clients.




    @Test
    void testEnregistrerExpeditionPETITClient(){
        //Création d'une commande
        var commande = service.creerCommande(ID_PETIT_CLIENT);

        //Récupération des quantités en stocks pour chaque produit de la commande avant MAJ
        var lAvant = commande.getLignes();
        var quantiteStockAvant = new ArrayList<Integer>();
        for(int i = 0; i < lAvant.size(); i++){
            quantiteStockAvant.add(lAvant.get(i).getProduit().getUnitesEnStock());
        }

        //Récupération des quantités en stocks pour chaque produit de la commande après MAJ + quantité de la commande
        var lApres = commande.getLignes();
        var quantiteStockApres = new ArrayList<Integer>();
        var quantites = new ArrayList<Integer>();
        for(int i = 0; i < lApres.size(); i++){
           quantiteStockApres.add(lApres.get(i).getProduit().getUnitesEnStock());
            quantites.add(lApres.get(i).getQuantite());
        }

        //Vérification du fonctionnement de la MAJ des stocks
        if(quantiteStockApres.size() == quantiteStockAvant.size()){
            for(int i = 0; i < quantiteStockAvant.size(); i++){
                assertEquals(quantiteStockAvant.get(i), quantiteStockApres.get(i) + quantites.get(i));
            }
        }
    }

    void testEnregistrerExpeditionGROSClient(){
        //Création d'une commande
        var commande = service.creerCommande(ID_GROS_CLIENT);

        //Récupération des quantités en stocks pour chaque produit de la commande avant MAJ
        var lAvant = commande.getLignes();
        var quantiteStockAvant = new ArrayList<Integer>();
        for(int i = 0; i < lAvant.size(); i++){
            quantiteStockAvant.add(lAvant.get(i).getProduit().getUnitesEnStock());
        }

        //Récupération des quantités en stocks pour chaque produit de la commande après MAJ + quantité de la commande
        var lApres = commande.getLignes();
        var quantiteStockApres = new ArrayList<Integer>();
        var quantites = new ArrayList<Integer>();
        for(int i = 0; i < lApres.size(); i++){
            quantiteStockApres.add(lApres.get(i).getProduit().getUnitesEnStock());
            quantites.add(lApres.get(i).getQuantite());
        }

        //Vérification du fonctionnement de la MAJ des stocks
        if(quantiteStockApres.size() == quantiteStockAvant.size()){
            for(int i = 0; i < quantiteStockAvant.size(); i++){
                assertEquals(quantiteStockAvant.get(i), quantiteStockApres.get(i) + quantites.get(i));
            }
        }
    }


    @Test
    void dateExpeditionBonJourPETITClient(){
        var commande = service.creerCommande(ID_PETIT_CLIENT);
        commande = service.enregistreExpédition(commande.getNumero());
        assertEquals(LocalDate.now(), commande.getEnvoyeele());
    }

    @Test
    void dateExpedictionBonJourGROSClient(){
        var commande = service.creerCommande(ID_GROS_CLIENT);
        commande = service.enregistreExpédition(commande.getNumero());
        assertEquals(LocalDate.now(), commande.getEnvoyeele());
    }
}
