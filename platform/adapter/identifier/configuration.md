# Identifier Configuration Guide (SpringBoot)

This is document for the identifier adapter. It contains settings and example descriptions.

이 문서는 식별자(TSID) 노드 번호 설정 규격과 설명예시를 포함하고 있습니다.

### 의존성

`adapter-web-servlet` / `adapter-web-reactive` 가 이 모듈을 가져오므로 **따로 추가하지 않아도 됩니다.**
web 어댑터가 이미 응답 id 생성에 TSID 를 쓰기 때문에 의존이 인위적이지 않고,
소비처가 추가를 잊어 조용히 노드 0 으로 도는 일을 막기 위해서이기도 합니다.

web 없는 애플리케이션(순수 배치·워커 등)은 `adapter-identifier` 를 직접 추가해야 노드 번호가 배선됩니다.
추가하지 않으면 기동은 되지만 **노드 0 으로 돌아 다른 서비스와 id 가 겹칠 수 있습니다.**

### application.yml
```yaml
platform:
  identifier:
    service-id: 7      # 필수. 서비스마다 고유한 번호 (0..63)
    instance-id: 0     # 선택. 기본 0 = 단일 인스턴스 (0..15)
    required: true     # 기본값. false 면 service-id 미설정을 허용한다(전환기용)
```

`service-id` 가 없으면 **기동에 실패합니다.** 조용히 0 으로 도는 것이 서비스 간 식별자 충돌의 원인이기 때문입니다.
전환기에 임시로 허용하려면 `required: false` 를 두십시오(그 경우 노드 0 으로 돕니다).

---

### 왜 노드 번호가 필요한가

TSID 는 `[timestamp 42][node 10][sequence 12]` 입니다. 마지막 12비트는 **밀리초당 시퀀스**라
한 프로세스 안에서는 충돌이 구조적으로 불가능합니다.

프로세스가 여럿이면 이야기가 다릅니다. 같은 노드 번호를 쓰는 두 프로세스는 같은 밀리초에
**같은 시퀀스에서 시작하므로 동일한 id 를 만듭니다**(확률이 아니라 확정입니다).
노드 번호는 그 프로세스들을 가르는 유일한 수단입니다.

### 노드 번호의 구성

```
node(10) = service-id(6)  |  instance-id(4)
           64개 서비스        서비스당 16 인스턴스
```

| | 스케일되나 | 배정 |
|---|---|---|
| `service-id` | 아니오 — 서비스는 늘지 않고 이미지도 설정도 다르다 | 정적. yaml 에 박고 배정표로 관리 |
| `instance-id` | 예 — 같은 이미지가 여러 개 뜬다 | 배포 시 주입 |

레이아웃은 **고정입니다.** 나중에 `instance-id` 를 자동 배정(임차)으로 바꿔도
이미 발급된 id 의 형식은 흔들리지 않습니다.

### service-id 배정

번호가 겹치면 두 서비스의 id 가 충돌하고 **DB 는 그걸 잡아주지 않습니다.**
해시로 자동 배정하지 마십시오 — 조용히 겹칩니다. 배정표를 두고 사람이 관리하십시오.

범위(0..63)를 벗어나면 기동에 실패합니다. 잘라내면 다른 서비스와 같은 번호가 되기 때문입니다.

### instance-id 주입 (스케일아웃)

단일 인스턴스면 설정하지 않아도 됩니다(0 으로 동작). 인스턴스를 늘리는 순간
**인스턴스마다 다른 값이 필요합니다.** 주지 않으면 전부 0 이 되어 id 가 겹칩니다.
그래서 부팅 로그에 해석된 번호를 남깁니다.

```
식별자 노드 번호 = 112 (service=7, instance=0). platform.identifier.instance-id 가 없어
단일 인스턴스로 본다 — 여러 인스턴스로 띄우려면 인스턴스마다 다른 값을 주입해야 id 가 겹치지 않는다.
```

환경변수로 주입할 수 있습니다(스프링 느슨한 바인딩).

```
PLATFORM_IDENTIFIER_SERVICE_ID=7
PLATFORM_IDENTIFIER_INSTANCE_ID=2
```

**Kubernetes**

- `StatefulSet` 은 파드 이름이 `svc-0`, `svc-1` 로 안정적이라 downward API 로 ordinal 을 뽑아 주입할 수 있습니다.
- `Deployment` 는 파드 이름이 랜덤 해시라 ordinal 이 없습니다. 파드 IP 해시는 조용히 겹치므로 쓰지 마십시오.
  이 경우는 인스턴스 번호를 런타임에 임차하는 방식이 필요합니다(미구현).

---

### 적용 시점

이 어댑터는 `EnvironmentPostProcessor` 로 동작합니다. **빈 생성보다 먼저** 설정을 심기 위해서입니다 —
자동설정이나 `@PostConstruct` 로 하면 그보다 앞서 id 를 만드는 빈이 하나라도 있을 때
그 빈만 노드 0 으로 발급받습니다.
