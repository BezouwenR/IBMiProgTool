# IBMiProgTool
IBM i Programming Tool

Created by Vladimír Župka, 2017

This project is not installed, it is ready to use as a Java application in the directory of the project name. The application requires Java SE 8 installed in PC.

User documentation can be found in the subdirectory "helpfiles" and it is also accessible under the menu Help in the running application.

This application replaces some functions of the System i Navigator, which ceased to work in Windows 10, especially simple transfer of files between IBM i and PC, displaying and editing of files and the like. In addition, the application enables compiling source members or stream files and finding errors from the compilation listing.

When editing source files, hihglighting of blocks in diffrent languages (e.g. if - endif, dow - enddo, etc. in RPG) can be set on or off.

When compiling, you can select the source type (e.g. RPGLE or RPG) compile command (e.g. CRTBNDRPG, CRTRPGMOD), set the correct library list, and other options. You can observe the result of the compilation in the spooled file.

Application programs are written in Java and require version Java SE 8. They cooperate with classes in IBM Toolbox for Java (or JTOpen). The application has been created and tested in systems macOS and Windows 10. Remote connection to the system IBM i, version 7.3 has been used.
