# Flows détaillés de la chaîne pharmaceutique publique (PNA/PRA)

## Organisation générale

Fournisseur
↓
PNA Centrale
↓
PRA ↔ PRA
↓
Hôpital / Centre de Santé / District

Important :

- Les PRA sont des représentations régionales de la PNA.
- Chaque PRA et PNA disposent de leur propre entrepôt.
- Chaque PRA et PNA disposent de leur propre stock.
- Les flux PNA → PRA et PRA → PRA sont des transferts internes.

## CAS 1 : La PNA achète chez un fournisseur

### Informations générales

```text
Source      : Fournisseur
Destination : PNA Centrale
Type        : ACHAT_FOURNISSEUR
```

### Workflow

```text
EN_ATTENTE_VALIDATION
↓
VALIDEE
↓
EN_TRANSIT
↓
EXPEDIEE
↓
PARTIELLEMENT_RECEPTIONNEE
↓
RECEPTIONNEE
```

### Processus métier détaillé

#### a) Prévision des besoins nationaux

La PNA analyse :

- les consommations historiques des PRA ;
- les tendances épidémiologiques ;
- les stocks actuels ;
- les seuils de sécurité ;
- les prévisions des programmes nationaux.

Exemple :

```text
Paracétamol : 50 millions de comprimés/an
Amoxicilline : 10 millions de comprimés/an
Ceftriaxone : 1 million de flacons/an
```

#### b) Lancement d'un appel d'offres

Plusieurs fournisseurs soumettent leurs offres.

```text
Laboratoire A
Produit : Amoxicilline 500 mg
Prix : 20 FCFA/comprimé

Laboratoire B
Produit : Amoxicilline 500 mg
Prix : 22 FCFA/comprimé
```

#### c) Signature du contrat

La PNA sélectionne un ou plusieurs fournisseurs.

#### d) Création du Bon de Commande

```text
BC-2026-0001

Fournisseur : Laboratoire A
Produit      : Amoxicilline 500 mg
Quantité     : 100 cartons
Prix unitaire: 200 000 FCFA/carton
Montant      : 20 000 000 FCFA
```

#### e) Validation interne

Validation par :

- Pharmacien Responsable ;
- Direction Générale ;
- Direction Financière.

#### f) Livraison

Le fournisseur expédie les produits.

```text
LOT A001 : 50 cartons
LOT A002 : 50 cartons
```

#### g) Réception

La PNA :

- contrôle les quantités ;
- vérifie les lots ;
- vérifie les dates de péremption ;
- contrôle les documents réglementaires.

#### h) Entrée en stock

```text
Stock PNA ++
Mouvement : ENTREE_ACHAT
```

---

## CAS 2 : Une PRA s'approvisionne auprès de la PNA

### Informations générales

```text
Source      : PNA Centrale
Destination : PRA Thiès
Type        : TRANSFERT_PNA_PRA
```

### Workflow

```text
EN_ATTENTE_VALIDATION
↓
VALIDEE
↓
EN_PREPARATION
↓
EN_TRANSIT
↓
EXPEDIEE
↓
PARTIELLEMENT_RECEPTIONNEE
↓
RECEPTIONNEE
```

### Processus détaillé

#### a) Analyse du stock régional

```text
Paracétamol

Stock actuel : 5 000 comprimés
Seuil minimal : 20 000
```

#### b) Création d'une demande

```text
DA-2026-045

PRA : Thiès
Produit : Paracétamol
Quantité : 100 boîtes
```

#### c) Validation PNA

La PNA vérifie :

- disponibilité ;
- quotas ;
- priorités sanitaires.
- historique derniere quantité commandé pour ces produits ;

#### d) Préparation

Préparation des lots selon la méthode FEFO.

#### e) Expédition

Documents générés :

- Bon de livraison ;
- Bon de transfert.

```text
Stock PNA --
```

#### f) Réception PRA

```text
Stock PRA ++
```

---

## CAS 3 : Une PRA demande un transfert à une autre PRA

### Informations générales

```text
Source      : PRA Thiès
Destination : PRA Kaolack
Type        : TRANSFERT_INTER_PRA
```

### Workflow

```text
EN_ATTENTE_VALIDATION_PNA
↓
VALIDEE
↓
EN_PREPARATION
↓
EN_TRANSIT
↓
EXPEDIEE
↓
PARTIELLEMENT_RECEPTIONNEE
↓
RECEPTIONNEE
```

### Processus détaillé

#### a) Détection d'une rupture

```text
PRA Kaolack

Paracétamol
Stock : 0
Seuil : 10 000
```

#### b) Recherche des disponibilités

```text
PRA Thiès : 50 000
PRA Dakar : 40 000
PRA Louga : 30 000
```

#### c) Demande de transfert

```text
TRF-2026-100

Destination : Kaolack
Source      : Thiès
Produit     : Paracétamol
Quantité    : 20 000 comprimés
```

#### d) Validation par la PRA source

La PRA source peut :

- accepter ;
- refuser ;

#### e) Préparation

Sélection des lots selon FEFO.

#### f) Expédition

```text
Stock PRA Source --
```

#### g) Réception

```text
Stock PRA Destination ++
```

---

## CAS 4 : Hôpital/Centre de Santé commande à une PRA

### Informations générales

```text
Source      : PRA Thiès
Destination : Hôpital Régional de Thiès
Type        : COMMANDE_STRUCTURE
```

### Workflow

```text
EN_ATTENTE_VALIDATION_PNA
↓
VALIDEE
↓
EN_PREPARATION
↓
EN_TRANSIT
↓
EXPEDIEE
↓
PARTIELLEMENT_RECEPTIONNEE
↓
RECEPTIONNEE
```

### Processus détaillé

#### a) Analyse des besoins

```text
Amoxicilline

Stock : 100 comprimés
Seuil : 5 000
```

#### b) Création commande

```text
CMD-2026-012

Amoxicilline : 50 boîtes
Paracétamol  : 100 boîtes
```

#### c) Validation PRA

La PRA vérifie :

- disponibilité ;
- impayés éventuels ;
- quotas.
- historique derniere quantité commandé pour ces produits ;

#### d) Préparation

Préparation selon FEFO.

#### e) Facturation

```text
Montant : 3 500 000 FCFA
```

#### f) Paiement

- paiement immédiat ;
- ou paiement différé.

#### g) Livraison

Documents :

- Facture ;
- Bon de livraison.

```text
Stock PRA --
```

#### h) Réception

```text
Stock Hôpital non gerer ici
```

---
