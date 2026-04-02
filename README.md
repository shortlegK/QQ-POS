# QQ POS – 飯糰店家點餐管理系統

> 一個為飯糰店家設計的後端 POS 系統，支援點餐、產品管理與員工權限控制。

[![CI](https://github.com/shortlegK/QQ-POS/actions/workflows/ci.yml/badge.svg)](https://github.com/shortlegK/QQ-POS/actions/workflows/ci.yml)
![Coverage](https://img.shields.io/badge/coverage-96%25-brightgreen)

## 線上 Demo
- **前端介面**：[qq-pos.vercel.app](https://qq-pos.vercel.app/)
- **API 文件（Swagger UI）**：[開啟 Swagger](https://qq-pos-production.up.railway.app/swagger-ui/index.html#/)

> **測試帳號**

 | 角色 | 帳號 | 密碼 |
 |------|------|------|
 | manager | `demomanager` | `Qq!pos3426` |
 | staff | `demostaff` | `Qq!pos3426` |

---

## 目錄

- [1. 專案簡介](#1-專案簡介)
- [2. 系統角色與權限設計](#2-系統角色與權限設計)
- [3. 架構概覽](#3-架構概覽)
- [4. 已實作功能](#4-已實作功能)
- [5. 測試策略](#5-測試策略)
- [6. CI/CD](#6-cicd)
- [7. 如何快速開始](#7-如何快速開始)
- [8. 技術棧與環境](#8-技術棧與環境)
- [9. 專案結構](#9-專案結構)
- [10. Roadmap](#10-roadmap)

## 1. 專案簡介

QQ-POS 是一個以飯糰店日常營運流程為情境的後端 POS 系統，透過 API 方式支援櫃檯點餐與後台管理操作。

本系統以實際營運流程為核心，系統模組規劃如下：

- 身分驗證模組  
  提供登入、登出與 JWT 驗證機制，作為所有操作的存取基礎。
- 員工管理模組  
  管理員工帳號、角色與啟用狀態，並限制不同角色可操作的功能範圍。
- 菜單管理模組  
  維護菜單分類、飯糰品項、加料選項與商品上下架狀態。
- 訂單管理模組  
  模擬實際點餐流程，建立訂單、調整訂單狀態。
- 營收統計模組  
  彙整每日與指定區間的營收資料。

## 2. 系統角色與權限設計

本系統區分為兩種使用角色，用以模擬實際營運中管理端與櫃檯操作端的權責差異。

- **manager**：可操作系統管理與維護相關功能（如員工管理、菜單管理），並可使用完整營運流程功能。
- **staff**：僅能使用與實際營運流程相關的功能（如登入與訂單操作），不具備任何系統設定或管理相關權限。

此角色區分同時也是測試設計的重要依據，用以驗證不同角色在相同 API 下，是否能被正確限制其操作行為，且不產生未預期的副作用。

---

## 3. 架構概覽

本專案以分層方式劃分系統責任，避免業務邏輯、資料存取與驗證規則混雜，讓核心行為能被獨立測試與驗證。

- **Controller**：負責接收請求與回傳 API 回應
- **Service**：集中處理業務邏輯、狀態判斷與權限規則
- **Mapper / Repository**：負責資料存取與持久化操作
- **Security / Filter**：處理身分驗證與存取控制
- **Exception Handler**：統一錯誤處理與回應格式
- **Test**：透過單元測試與整合測試驗證核心行為

---

## 4. 已實作功能

### 身分驗證（Login / Logout）
- 實作登入與登出流程
- 處理常見異常情境（資料缺失、帳號不存在、密碼錯誤）。
- 定義統一的錯誤回應格式，作為系統行為驗證與測試設計的基礎。

### 員工管理
- 實作查詢、新增、編輯、啟用 / 停用等功能。
- 於操作流程中加入權限檢查與資料驗證機制。
- 處理重複帳號與資料格式錯誤等例外情境。

### 產品管理
- 實作產品的新增、查詢、編輯與上/下架狀態調整等功能。
- 提供依產品名稱、類型與上架狀態篩選的分頁查詢。
- 處理產品名稱重複與資料格式不符等例外情境。

### 選項管理
- 實作選項的新增、查詢、編輯與上/下架狀態調整等功能。
- 於建立時依選項類型進行合法性驗證（如加料類及去除配料類別的選項不可設為預設）。
- 處理選項名稱重複與類型設定衝突等例外情境。

### 訂單管理
- 實作訂單的建立、查詢、修改與狀態更新等完整流程。
- 於建立訂單時驗證商品上架狀態、選項有效性與必填項目完整性。
- 提供商品目錄查詢介面，整合可訂購品項與對應細節選項資訊。
- 處理商品下架、選項不可用、選項重複設定與資料格式錯誤等例外情境。

### 帳號管理
- 實作登入帳號的密碼變更功能。
- 於執行變更前驗證舊密碼，確保操作者身分正確後方可進行更新。
- 處理舊密碼錯誤與資料格式不符等例外情境。

### API 文件（Swagger UI）

以下為目前已完成模組的 API 文件畫面，  
用以展示系統模組劃分與權限保護狀態。

<img src="./images/swagger-api.jpg" alt="Swagger API Overview" width="800" />

---

## 5. 測試策略

> 目前測試覆蓋率達 **96%**，CI 設定門檻為 85%。
<details>
<summary>查看 JaCoCo 覆蓋率報告</summary>

![JaCoCo Report](./images/jacoco-report.png)

</details>

本專案針對所有已完成的功能模組皆撰寫對應的單元測試與整合測試，
以確保各模組的核心行為、權限控制與例外處理皆符合預期。

測試設計以角色行為為主要驗證維度，
涵蓋不同角色在各種操作情境下的權限限制、狀態流轉與錯誤回應。

測試內容包含：
- 未授權操作是否被正確阻擋，且不產生副作用
- 狀態變更後，其存取行為是否立即受到影響
- 例外情境是否能回傳一致且可預期的錯誤回應

### 代表測試案例

- **權限邊界：未授權操作不產生副作用**  
  staff 嘗試建立員工或變更狀態時，預期回傳 403，並於後續查詢中確認資料未被建立或狀態未被改動。

- **狀態流轉：停用後存取行為立即受限**  
  驗證帳號由 ACTIVE → INACTIVE → ACTIVE 的狀態切換，並確認帳號停用後，即使持有既有的 JWT Token，也會因帳號狀態檢查而被拒絕存取。

- **例外處理：錯誤回應一致且可預期**  
  模擬資料庫發生 DuplicateKeyException，由 Service 層處理並回傳對應的業務錯誤，確保 API 回傳一致且可預期的錯誤碼與訊息。

---

## 6. CI/CD

本專案透過 GitHub Actions 實作自動化測試與部署流程，
每次 push 或 PR 到 `master` / `develop` 分支時自動觸發。

**Pipeline 流程**

```
push / PR → [test] → [coverage] → [deploy]（僅 master）
```

| Stage | 說明 |
|-------|------|
| **test** | 啟動 MySQL container，執行全部單元與整合測試 |
| **coverage** | 驗證 JaCoCo 測試覆蓋率門檻 ≥ 0.85，目前實際覆蓋率為 **96%** |
| **deploy** | 通過前兩關後，自動部署到 Railway（僅限 master 分支） |

---

## 7. 如何快速開始

### 7.1 環境需求
- Java 17
- Spring Boot 3.x
- MySQL 8+

### 7.2 資料庫準備

本專案使用 MySQL 作為資料庫，請先建立資料庫並匯入資料表結構。

#### 7.2.1 建立資料庫（範例：`qq_pos_dev`）
```bash
mysql -u <user> -p -e "CREATE DATABASE IF NOT EXISTS qq_pos_dev DEFAULT CHARACTER SET utf8mb4;"
```
#### 7.2.2 匯入資料表結構
```bash
mysql -u <user> -p qq_pos_dev < ./db/qqPosDB.sql
```

#### 7.2.3 測試環境（範例：`qq_pos_test`）
```bash
mysql -u <user> -p -e "CREATE DATABASE IF NOT EXISTS qq_pos_test DEFAULT CHARACTER SET utf8mb4;"
mysql -u <user> -p qq_pos_test < ./db/qqPosDB.sql
```

#### 7.2.4 設定環境變數（.env）

```bash
cp .env.example .env
```
將 `.env.example` 複製為 `.env`，並依本機環境修改資料庫帳號密碼與 JWT key。

### 7.3 啟動專案

- 開發環境啟動（dev）：
```bash
./gradlew bootRun --args='--spring.profiles.active=dev'
```
啟動後可透過 API 進行功能驗證，Swagger 文件位於 http://localhost:8080/swagger-ui/index.html

- （可選）建立開發用 demo 帳號（dev）：
```bash
./gradlew bootRun --args='--spring.profiles.active=dev --app.seed-demo=true'
```
demo 帳號：  
manager：`demomanager` / `Qq!pos3426`  
staff：`demostaff` / `Qq!pos3426`

- 測試執行：
  ```bash
  ./gradlew test
  ```
測試環境會自動建立測試所需資料，且不影響開發或正式環境。

---

## 8. 技術棧與環境

- **語言 / 框架**：Java 17、Spring Boot 3.x
- **資料庫 / 持久層**：MySQL、MyBatis（PageHelper 分頁）
- **安全性**：Spring Security、JWT
- **測試**：JUnit 5、Mockito、Spring Security Test
- **文件 / 工具**：Gradle、SpringDoc（OpenAPI 3 / Swagger UI）

---

## 9. 專案結構

### 9.1 主要程式（src/main/java）
```text
com.qqriceball
├── annotation      # 自訂註解，輔助共用行為
├── aspect          # AOP 切面邏輯
├── common          # 通用元件（例外、回應格式、工具類）
│   ├── exception
│   ├── properties
│   ├── result
│   └── utils
├── config          # 系統設定（Security、JWT、Swagger 等）
├── controller      # API 控制層，負責請求與回應
├── enumeration     # 系統常數與狀態列舉
├── handler         # 全域例外處理
├── mapper          # MyBatis Mapper（SQL 定義於 resources）
├── model           # 資料模型
│   ├── dto         # 請求資料
│   ├── entity      # 資料庫實體
│   └── vo          # 回應資料
└── service         # 業務邏輯層
```

### 9.2 測試（src/test/java）
```text
com.qqriceball
├── unit            # 單元測試，驗證單一類別的業務邏輯與錯誤處理
│   ├── controller  # Controller 層的請求與回應行為
│   └── service     # Service 層的業務規則與例外處理
└── integration     # 整合測試，驗證完整流程與權限行為
└── testData    # 共用測試資料
└── utils       # 測試工具類
```

---
## 10. Roadmap

以下列出目前已完成與預計實作的功能項目。

- [x] 專案基礎建置（Spring Boot + MyBatis）
- [x] 基本身分驗證流程（登入 / 登出）
- [x] JWT 驗證與角色權限區分（manager / staff）
- [x] 員工管理功能
  - [x] 新增、查詢、編輯員工
  - [x] 啟用/停用員工帳號
- [x] 產品管理
  - [x] 新增、編輯產品
  - [x] 查詢產品
  - [x] 調整上架/下架狀態
- [x] 選項管理
  - [x] 新增、編輯選項
  - [x] 查詢選項
  - [x] 調整上架/下架狀態
- [x] 訂單管理
  - [x] 建立訂單
  - [x] 修改訂單
  - [x] 訂單狀態變更
  - [x] 查詢訂單(依訂單編號、狀態、日期等條件)
- [x] 帳號管理
  - [x] 變更密碼
- [ ] 營收統計
  - [ ] 每日營收統計
  - [ ] 依日期區間查詢營收
  - [ ] 營收明細查詢