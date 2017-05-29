# IBMiProgTool
IBM i Programming Tool

Created by Vladimír Župka, 2017

vzupka@gmail.com


This is a NetBeans project. The project is not installed, it is ready to use as a Java application in the directory of the project. The application requires Java SE 8 installed in PC.

User documentation can be found in the subdirectory "helpfiles" and it is also accessible under the menu Help in the running application.

This application replaces some functions of the System i Navigator, which ceased to work in Windows 10, especially transfer of files between IBM i and PC, displaying and editing of files and the like. In addition, the application enables compiling source members or stream files and finding errors from the compilation listing.

When editing source files, hihglighting of blocks in diffrent languages (e.g. if - endif, dow - enddo, etc. in RPG) can be set on or off.

When compiling, you can select the source type (e.g. RPGLE or RPG), select compile command (e.g. CRTBNDRPG, CRTRPGMOD), set the correct library list, and other options. You can observe the result of the compilation in the spooled file.

Application programs are written in Java and require version Java SE 8. They cooperate with classes in IBM Toolbox for Java (or JTOpen). The classes require "host servers" running in IBM i and profile QUSER enabled.

The application has been created and tested in systems macOS and Windows 10. Remote connection to the system IBM i, version 7.3 has been used.

Start the application by clicking on the IBMiProgTool.jar file.

- - - - - - - - - - 

Toto je NetBeans projekt. Projekt se neinstaluje, je okamžitě k použití jako Java aplikace v adresáři projektu. Aplikace vyžaduje Javu SE 8 instalovanou v PC.

Uživatelskou dokumentaci lze nalézt v podadresáři "helpfiles" a je také přístupná pod nabídkou Help v běžící aplikaci.

Tato aplikace nahrazuje některé funkce programu System i Navigator, které přestaly fungovat v systému Windows 10, zejména přesuny souborů mezi IBM i a PC, jejich zobrazování a editaci. Aplikace navíc umožňuje kompilovat programové zdrojové soubory, editovat je a odhalovat chyby z protokolu o kompilaci. 

Při editaci zdrojových souborů může být zapnuto nebo vypnuto zvýraznění bloků v různých jazycích (např. if - endif, dow - enddo, atd. v RPG).

Při kompilaci lze vybrat zdrojový typ (např. RPGLE nebo RPG), vybrat kompilační příkaz (např. CRTBNDRPG, CRTRPGMOD), nastavit správný seznam knihoven a další volby. Výsledek kompilace lze pozorovat v tiskovém souboru (spooled file).

Aplikační programy jsou napsány v jazyku Java a vyžadují verzi Java SE 8. Spolupracují s třídami v soustavě IBM Toolbox for Java (nebo JTOpen). Tyto třídy vyžadují, aby v IBM i běžely "host servery" a aby byl aktivován profil QUSER.

Aplikace byla vytvořena a testována v systémech macOS a Windows 10. Bylo použito spojení se systémem IBM i verze 7.3.

Aplikace se spouští poklepáním na soubor IBMiProgTool.jar.

