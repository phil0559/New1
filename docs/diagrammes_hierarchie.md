# Diagrammes hiérarchiques – Conteneur 81

Chaque situation illustre la règle suivante :

* Les éléments simples sont listés avant les conteneurs à l'intérieur d'un même parent.
* Dès qu'un conteneur possède du contenu, il reçoit un rang numérique.
* Les rangs sont exclusivement composés de numéros successifs séparés par des points.

## Situation 1 – Organisation initiale
```
81 (conteneur, enfants=5)
├─ 81.1 – Albums (élément)
├─ 81.2 – Télécommande (élément)
├─ 81.3 – Casque (élément)
├─ 81.4 – Boîte 81-A (conteneur, enfants=2)
│  ├─ 81.4.1 – Figurine (élément)
│  └─ 81.4.2 – Sac 81-A1 (conteneur, enfants=1)
│     └─ 81.4.2.1 – Chargeur (élément)
└─ 81.5 – Boîte 81-B (conteneur, enfants=1)
   └─ 81.5.1 – Clés (élément)
```

## Situation 2 – Déplacement de 81.3 dans 81.4
```
81 (conteneur, enfants=4)
├─ 81.1 – Albums (élément)
├─ 81.2 – Télécommande (élément)
├─ 81.4 – Boîte 81-A (conteneur, enfants=3)
│  ├─ 81.4.1 – Figurine (élément)
│  ├─ 81.4.2 – Casque (élément)
│  └─ 81.4.3 – Sac 81-A1 (conteneur, enfants=1)
│     └─ 81.4.3.1 – Chargeur (élément)
└─ 81.5 – Boîte 81-B (conteneur, enfants=1)
   └─ 81.5.1 – Clés (élément)
```

## Situation 3 – La figurine passe dans 81.5
```
81 (conteneur, enfants=4)
├─ 81.1 – Albums (élément)
├─ 81.2 – Télécommande (élément)
├─ 81.4 – Boîte 81-A (conteneur, enfants=2)
│  ├─ 81.4.1 – Casque (élément)
│  └─ 81.4.2 – Sac 81-A1 (conteneur, enfants=1)
│     └─ 81.4.2.1 – Chargeur (élément)
└─ 81.5 – Boîte 81-B (conteneur, enfants=2)
   ├─ 81.5.1 – Clés (élément)
   └─ 81.5.2 – Figurine (élément)
```

## Situation 4 – Sac remonté au niveau de 81 et retour de 81.3
```
81 (conteneur, enfants=5)
├─ 81.1 – Albums (élément)
├─ 81.2 – Télécommande (élément)
├─ 81.3 – Casque (élément)
├─ 81.4 – Boîte 81-A (conteneur, enfants=0)
├─ 81.5 – Sac 81-A1 (conteneur, enfants=1)
│  └─ 81.5.1 – Chargeur (élément)
└─ 81.6 – Boîte 81-B (conteneur, enfants=2)
   ├─ 81.6.1 – Clés (élément)
   └─ 81.6.2 – Figurine (élément)
```
