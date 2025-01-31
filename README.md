
```text
     ______  ___  __   ____________    
 __ / / __ \/ _ )/ /  / __/ __/ __/    
/ // / /_/ / _  / /__/ _/_\ \_\ \      
\___/\____/____/____/___/___/___/_____ 
  /  |/  / _ | / |/ / _ |/ ___/ __/ _ \
 / /|_/ / __ |/    / __ / (_ / _// , _/
/_/  /_/_/ |_/_/|_/_/ |_\___/___/_/|_|                               
```
# JOBLESS PASSWORD MANAGER

⚠️ **DISCLAIMER: THIS IS AN UNTESTED EDUCATIONAL PROJECT**  
**DO NOT USE IN PRODUCTION OR FOR SENSITIVE DATA**. 

This code may contain critical security flaws, including vulnerabilities to memory dumping attacks, improper cryptographic implementations, or other risks. Always review with a security expert before considering any use.
See Critical Security Considerations for more information.

---

## Description  
This is a more than basic local password manager prototype in Java.

This project's scope is to create a "well-secured" password manager using modern recommendations. The development of this project is still ongoing, so a big part of the functionality is not implemented yet.

---

## ⚠️ Critical Security Considerations  
This implementation has **NOT** been audited or tested rigorously. Known/potential risks may include:  
- **Memory Dumping Vulnerabilities**: Sensitive data (e.g., master passwords, decrypted keys) may persist in memory.  
- **Lack of Secure Password Policy**: Nothing stops users from using insecure passwords.  
- **Master password theft**: If your password and database were stolen, nothing will stop an attacker from decrypting the whole database.

---

## Features (Experimental)  
- AES-GCM encryption/decryption of entries.  
- SQLite3-based storage for encrypted data. 
   - We are encrypting not only passwords but also service names and usernames.
- Master password authentication. 

---

## Roadmap
- [x] AES-GCM encryption/decryption
- [x] CLI user interface
- [ ] Code testing
- [ ] Password policy
- [    ] MFA implementation
- [ ] Better UI / Browser extension

*This is a basic roadmap, some points may be skipped or changed in the near future!*

---

## WARNING
- Avoid entering real passwords. Use dummy data only.
- Terminate the app immediately after testing to reduce memory exposure.

---


## Contributing
We are looking for security audits, so if you find any vulnerabilities, please contact dmitri.matetski@gmail.com before creating **issue**, to ensure this vulnerability will be patched ASAP.

---

## License
This project is [MIT licensed](LICENSE) with **NO WARRANTY**. Use at your own risk.

---

## References for Secure Practices
- [OWASP Password Storage Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html)
- [NIST Guidelines for AES-GCM](https://csrc.nist.gov/publications/detail/sp/800-38d/final)