# Stockfela – Request Flow Diagrams

Render with Mermaid (GitHub, VS Code, etc.).

---

## 1. Authentication Flow

```mermaid
sequenceDiagram
    participant C as Client
    participant F as AuthTokenFilter
    participant AC as AuthController
    participant AM as AuthenticationManager
    participant US as UserService
    participant DB as Database

    Note over C,DB: REGISTRATION
    C->>AC: POST /api/auth/register { username, email, password, ... }
    AC->>US: registerUser(RegisterRequest)
    US->>DB: existsByUsername / existsByEmail (duplicate check)
    DB-->>US: false (no duplicate)
    US->>DB: userRepository.save(user) with BCrypt-hashed password
    DB-->>US: saved User entity
    US-->>AC: RegisterResponse
    AC-->>C: 201 Created { id, username, email, fullName, role }

    Note over C,DB: LOGIN
    C->>AC: POST /api/auth/login { username, password }
    AC->>AM: authenticate(UsernamePasswordAuthenticationToken)
    AM->>US: loadUserByUsername(username)
    US->>DB: findByUsername(username)
    DB-->>US: User entity
    US-->>AM: UserDetails (username + BCrypt hash + authorities)
    AM->>AM: BCrypt.matches(rawPassword, hash)
    AM-->>AC: Authentication object
    AC->>AC: jwtUtilities.generateTokenFromUsername(userDetails)
    AC-->>C: 200 OK { jwtToken, username, roles }
```

---

## 2. Authenticated Request Flow

```mermaid
sequenceDiagram
    participant C as Client
    participant F as AuthTokenFilter
    participant SC as SecurityContext
    participant CTRL as Controller
    participant SVC as Service

    C->>F: ANY /api/** with Authorization: Bearer <jwt>
    F->>F: jwtUtilities.getJwtForHeader(request)
    F->>F: jwtUtilities.validateJwtToken(jwt)
    alt JWT valid
        F->>F: jwtUtilities.getUserNameFromJwtToken(jwt)
        F->>SVC: userDetailsService.loadUserByUsername(username)
        SVC-->>F: UserDetails
        F->>SC: setAuthentication(UsernamePasswordAuthenticationToken)
        F->>CTRL: filterChain.doFilter() — request proceeds
    else JWT invalid / missing
        F->>CTRL: filterChain.doFilter() — no auth in context
        CTRL-->>C: 401 Unauthorized (AuthEntryPointJWT)
    end
```

---

## 3. Create Group Flow

```mermaid
sequenceDiagram
    participant C as Client
    participant GC as GroupController
    participant SC as SecurityContext
    participant US as UserService
    participant SGS as SavingGroupService
    participant DB as Database

    C->>GC: POST /api/groups { name, monthlyContribution, cycleMonths }
    GC->>SC: @AuthenticationPrincipal -> username
    GC->>US: findByUsername(username)
    US->>DB: SELECT * FROM users WHERE username = ?
    DB-->>US: User entity
    US-->>GC: User (creator)
    GC->>SGS: createGroup(group, creator)
    SGS->>DB: INSERT INTO savings_groups ...
    DB-->>SGS: SavingsGroup (id assigned)
    SGS->>DB: INSERT INTO group_members (group_id, user_id, payout_order=1)
    DB-->>SGS: GroupMember
    SGS-->>GC: SavingsGroup
    GC-->>C: 201 Created { success: true, group: {...} }
```

---

## 4. Payout Cycle Flow

```mermaid
sequenceDiagram
    participant C as Client
    participant GC as GroupController
    participant SGS as SavingGroupService
    participant PCS as PayoutCycleService
    participant DB as Database

    C->>GC: POST /api/groups/{groupId}/payout-cycle
    GC->>SGS: startNewPayoutCycle(groupId)
    SGS->>DB: findNextPayoutRecipient(groupId) ORDER BY payout_order ASC WHERE has_received_payout=false
    DB-->>SGS: GroupMember (next recipient)
    SGS->>DB: countByGroup(group) -> memberCount
    SGS->>SGS: totalAmount = monthlyContribution * memberCount
    SGS->>PCS: savePayoutCycle(payoutCycle)
    PCS->>DB: INSERT INTO payout_cycles ...
    DB-->>PCS: PayoutCycle saved
    SGS->>PCS: createContributionForPayoutCycle(savedCycle)
    loop For each group member
        PCS->>DB: INSERT INTO contributions (status=PENDING)
    end
    SGS->>DB: UPDATE group_members SET has_received_payout=true WHERE id=?
    SGS->>DB: UPDATE savings_groups SET current_cycle=current_cycle+1
    SGS-->>GC: PayoutCycle
    GC-->>C: 200 OK { success: true, message: "Payout cycle started" }
```

---

## 5. Record Payment Flow

```mermaid
sequenceDiagram
    participant C as Client
    participant PC as PayoutController
    participant PCS as PayoutCycleService
    participant DB as Database

    C->>PC: POST /api/payouts/{cycleId}/pay { userId, amount }
    PC->>PCS: recordPayment(cycleId, userId, amount)
    PCS->>DB: SELECT * FROM contributions WHERE payout_cycle_id=? AND user_id=?
    DB-->>PCS: Contribution (PENDING)
    PCS->>DB: UPDATE contributions SET status=PAID, paid_at=NOW(), amount=?
    PCS->>DB: COUNT contributions WHERE payout_cycle_id=? AND status=PENDING
    alt All contributions PAID (pendingCount = 0)
        PCS->>DB: UPDATE payout_cycles SET status=COMPLETED
    end
    PCS-->>PC: Contribution (PAID)
    PC-->>C: 200 OK { success: true, contribution: {...} }
```
