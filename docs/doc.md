# Document technique des fonctionnalités – SEN PharmaFlow

## 1. Présentation

SEN PharmaFlow est une plateforme de gestion de la chaîne d'approvisionnement pharmaceutique publique. Elle permet de gérer l'ensemble du cycle de vie des médicaments, depuis leur acquisition auprès des fournisseurs jusqu'à leur distribution aux structures sanitaires, tout en garantissant une traçabilité complète des stocks, des lots et des mouvements.

Le système est construit autour de la PNA (Pharmacie Nationale d'Approvisionnement), des PRA (Pharmacies Régionales d'Approvisionnement) et des structures sanitaires clientes.

---

# 2. Gestion des utilisateurs et des accès

Le système dispose d'une gestion des utilisateurs basée sur des rôles.

## Fonctionnalités

- Authentification sécurisée.
- Gestion des rôles et des permissions.
- Gestion des comptes utilisateurs.
- Activation ou désactivation d'un utilisateur.
- Affectation d'un utilisateur à :
  - une PNA ;
  - une PRA ;
  - une structure sanitaire.

---

# 3. Gestion des structures sanitaires

Le système permet de gérer les établissements de santé bénéficiant des médicaments.

## Fonctionnalités

- Création d'une structure sanitaire.
- Validation des demandes d'adhésion.
- Modification des informations.
- Désactivation d'une structure.
- Affectation à une région et à un PRA.

---

# 4. Gestion des entrepôts

Le système permet de gérer les entrepôts de stockage.

## Fonctionnalités

- Gestion de la PNA.
- Gestion des PRA.
- Consultation des informations d'un entrepôt.
- Gestion des responsables d'entrepôt.

---

# 5. Gestion des fournisseurs

La PNA peut gérer les fournisseurs de médicaments.

## Fonctionnalités

- Création d'un fournisseur.
- Modification des informations.
- Désactivation d'un fournisseur.
- Consultation de l'historique des commandes.

---

# 6. Gestion du catalogue des médicaments

Le système centralise le référentiel national des médicaments.

## Fonctionnalités

- Création d'un médicament.
- Modification des informations.
- Désactivation d'un médicament.
- Gestion :
  - de la DCI ;
  - du nom commercial ;
  - du dosage ;
  - de la forme pharmaceutique ;
  - de la voie d'administration ;
  - de la famille thérapeutique ;
  - des températures de conservation ;
  - des délais d'approvisionnement ;
  - des programmes de santé ;
  - de l'obligation d'ordonnance.

- Définition des seuils minimum et maximum de stock.

---

# 7. Gestion des conditionnements

Chaque médicament peut posséder plusieurs niveaux de conditionnement.

## Fonctionnalités

- Création d'un conditionnement.
- Modification.
- Suppression.
- Définition de l'unité de base.
- Gestion des conversions entre unités.

---

# 8. Gestion des lots

Le système assure la traçabilité complète des lots.

## Fonctionnalités

- Création d'un lot.
- Identification par numéro de lot.
- Association à un médicament.
- Association à un fournisseur.
- Gestion des dates de fabrication.
- Gestion des dates de péremption.
- Gestion des prix d'achat.
- Gestion des prix de vente.
- Suivi du statut du lot.
- Application automatique de la règle FEFO.

---

# 9. Gestion des stocks

Chaque entrepôt possède son propre stock.

## Fonctionnalités

- Consultation des stocks.
- Réservation automatique des quantités.
- Consultation du stock disponible.
- Consultation des commandes en attente d'approvisionnement.
- Visualisation des alertes de rupture.
- Visualisation des seuils de sécurité.
- Consultation des stocks par :
  - médicament ;
  - lot ;
  - entrepôt ;
  - région.

---

# 10. Gestion des commandes

Le système centralise l'ensemble des flux logistiques.

## Types de commandes

- Achat fournisseur → PNA.
- Réapprovisionnement PNA → PRA.
- Transfert PRA → PRA.
- Distribution PRA → Structure sanitaire.

## Fonctionnalités

- Création d'une commande.
- Modification d'une commande.
- Validation.
- Affectation des lots.
- Modification des quantités.
- Gestion des commentaires.
- Consultation du détail.
- Consultation du suivi.
- Historisation des changements d'état.

---

# 11. Gestion des réceptions

Le système gère la réception des commandes.

## Fonctionnalités

- Réception totale.
- Réception partielle.
- Refus partiel.
- Gestion des commentaires.
- Génération automatique des entrées en stock.
- Mise à jour des quantités restantes à réceptionner.

---

# 12. Gestion des transferts inter-PRA

Le système permet aux PRA de s'entraider.

## Fonctionnalités

- Consultation du stock des autres PRA.
- Création d'une demande de transfert.
- Validation.
- Expédition.
- Réception.
- Suivi complet du transfert.

---

# 13. Gestion des mouvements de stock

Toute modification du stock est historisée.

## Types de mouvements

- Entrée fournisseur.
- Réception d'un transfert.
- Sortie vers une structure sanitaire.
- Sortie vers une PRA.
- Ajustement.
- Péremption.
- Casse.
- Vol.
- Retour.

## Fonctionnalités

- Consultation de l'historique.
- Filtrage.
- Recherche.
- Traçabilité utilisateur.

---

# 14. Gestion des inventaires

Le système permet la réalisation d'inventaires physiques.

## Fonctionnalités

- Création d'un inventaire.
- Comptage physique.
- Comparaison avec le stock théorique.
- Calcul automatique des écarts.
- Validation de l'inventaire.
- Génération automatique des mouvements d'ajustement.

---

# 15. Pharmacovigilance

Les structures sanitaires peuvent déclarer des incidents liés aux médicaments.

## Fonctionnalités

- Déclaration d'un cas.
- Association à un lot.
- Consultation de l'historique.
- Suivi du traitement de la déclaration.

---

# 16. Tableau de bord

Le système fournit des indicateurs de pilotage.

## Indicateurs

- Valeur totale des stocks.
- Nombre de commandes.
- Commandes en attente.
- Commandes en transit.
- Commandes réceptionnées.
- Médicaments en rupture.
- Lots proches de la péremption.
- Quantités en commande.
- Activité des PRA.
- Activité des structures sanitaires.

---

# 17. Alertes

Le système génère automatiquement des notifications.

## Alertes

- Stock minimum atteint.
- Rupture de stock.
- Péremption à 12 mois.
- Péremption à 6 mois.
- Péremption à 3 mois.
- Péremption à 1 mois.
- Commande en retard.
- Réception en attente.
- Inventaire à réaliser.

---

# 18. Traçabilité

Toutes les opérations importantes sont historisées.

Le système conserve notamment :

- les changements de statut des commandes ;
- les mouvements de stock ;
- les utilisateurs ayant réalisé chaque opération ;
- les dates et heures des actions ;
- les commentaires associés aux traitements.
