package comptoirs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.jdbc.Sql;

import comptoirs.dao.CategorieRepository;
import comptoirs.entity.Categorie;
import lombok.extern.log4j.Log4j2;

@Log4j2 // Génère le 'logger' pour afficher les messages de trace
@DataJpaTest
class CategorieRepositoryTest {
	@Autowired
	private CategorieRepository categoryDAO;

	@Test
	@Sql("small_data.sql")
	void compterLesEntites() {
		log.info("Compter les entités");

		long nombre = categoryDAO.count(); // 'count' donne le nombre d'enregistrements

		assertEquals(2, nombre, "Le jeu de test contient 2 catégories");
	}

	@Test
	@Sql("small_data.sql")
	void listerLesEntites() throws JsonProcessingException {
		log.info("Lister les entités");

		List<Categorie> liste = categoryDAO.findAll(); // Renvoie la liste des entités dans la table

		assertFalse(liste.isEmpty(), "Il y a des catégories dans le jeu de test");

		log.info("Liste des entités: {}", liste);
	}
	
	@Test
	@Sql("small_data.sql")
	void listerCustomQuery() throws JsonProcessingException {
		log.info("Chercher des entités avec une requête 'custom' Spring");
		
		String substring = "on";

		List<Categorie> liste = categoryDAO.findByLibelleContaining(substring);
		log.info("Entités trouvées: {}", liste);
		
		assertEquals(2, liste.size(), "Il y a deux catégories dont le libellé contient la sous-chaine");
		
		substring = "xx";
		assertTrue(categoryDAO.findByLibelleContaining(substring).isEmpty(), "Aucun libellé de catégorie ne contient cette sous-chaine"); 	
	}
	
	@Test
	@Sql("small_data.sql")
	void touverParCle() throws JsonProcessingException {
		log.info("Trouver une entité par sa clé");

		int codePresent = 1;
		Optional<Categorie> resultat = categoryDAO.findById(codePresent);
		// On s'assure qu'on trouvé le résultat
		assertTrue(resultat.isPresent(), "Cette catégorie existe");
		Categorie c = resultat.get();
		assertEquals("Boissons", c.getLibelle());

		log.info("Entité trouvée: {}", c);
	}

	@Test
	@Sql("small_data.sql")
	void entiteInconnue()  {
		log.info("Chercher une entité inconnue");
		int codeInconnu = 99;

		Optional<Categorie> resultat = categoryDAO.findById(codeInconnu);

		assertFalse(resultat.isPresent(), "Cette catégorie n'existe pas");

	}

	@Test
	@Sql("small_data.sql")
	void creerUneEntite() throws JsonProcessingException {
		log.info("Créer une entité");
		Categorie nouvelle = new Categorie();
		nouvelle.setLibelle("essai");
		nouvelle.setDescription("essai");
		assertNull(nouvelle.getCode(), "L'entité n'a pas encore de clé");
		categoryDAO.save(nouvelle); // 'save' enregistre l'entite dans la base
		Integer nouvellecle = nouvelle.getCode(); // La clé a été auto-générée lors de l'enregistrement
		assertNotNull(nouvellecle, "Une nouvelle clé doit avoir été générée");
		log.info("Nouvelle entité: {}", nouvelle);
	}

	@Test
	@Sql("small_data.sql")
	void modifierEntite() throws JsonProcessingException {
		log.info("Modifier une entité");

		int codePresent = 1;
		String ancienLibelle = "Boissons";
		String nouveauLibelle = "Libellé modifié";
		Categorie c = categoryDAO.getOne(codePresent); // On peut utiliser getOne si on est sur qu'elle existe
		assertEquals(ancienLibelle, c.getLibelle());
		// On change l'entité
		c.setLibelle(nouveauLibelle);
		// On l'enregistre dans la base
		categoryDAO.save(c);
		// On vérifie que l'enregistrement s'est bien passé
		Categorie recherche = categoryDAO.findByLibelle(nouveauLibelle);
		assertEquals(codePresent, recherche.getCode(), "La catégorie doit avoir été modifiée");
	}

	@Test
	@Sql("small_data.sql")
	void erreurCreationEntite() {
		log.info("Créer une entité avec erreur");
		Categorie nouvelle = new Categorie();
		nouvelle.setLibelle("Boissons");  // Ce libellé existe dans le jeu de test
		nouvelle.setDescription("essai");
		try { // L'enregistreement peut générer des exceptions (ex : violation de contrainte d'intégrité)
			categoryDAO.save(nouvelle);
			fail("Les libellés doivent être tous distincts, on doit avoir une exception");
		} catch (DataIntegrityViolationException e) {
			// Si on arrive ici c'est normal, on a eu l'exception attendue
		}

		assertNull(nouvelle.getCode(), "La clé n'a pas été générée, l'entité n'est pas enregistrée");
	}

	@Test
	@Sql("small_data.sql")
	void onNePeutPasDetruireUneCategorieQuiADesProduits() {
		log.info("Détruire une catégorie avec des produits");
		Categorie boissons = categoryDAO.getOne(1);
		assertEquals("Boissons", boissons.getLibelle());
		// Il y a des produits dans la catégorie 'Boissons'
		assertFalse(boissons.getProduits().isEmpty());
		// Si on essaie de détruire cette catégorie, on doit avoir une exception
		// de violation de contrainte d'intégrité
		assertThrows(DataIntegrityViolationException.class, () -> {
			categoryDAO.delete(boissons);
			categoryDAO.flush();
		});
	}
}
