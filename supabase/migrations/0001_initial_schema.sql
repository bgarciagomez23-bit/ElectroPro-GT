-- ElectroPro GT: initial multi-tenant schema.
-- Review before applying to a newly created Supabase project.
create extension if not exists pgcrypto;

create table if not exists public.profiles (
  id uuid primary key references auth.users(id) on delete cascade,
  display_name text,
  created_at timestamptz not null default now()
);

create table if not exists public.projects (
  id uuid primary key default gen_random_uuid(),
  owner_id uuid not null references auth.users(id) on delete cascade,
  name text not null,
  client_name text,
  location text,
  status text not null default 'active'
    check (status in ('active','paused','closed')),
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create table if not exists public.calculations (
  id uuid primary key default gen_random_uuid(),
  project_id uuid not null references public.projects(id) on delete cascade,
  owner_id uuid not null references auth.users(id) on delete cascade,
  calculation_type text not null,
  algorithm_version text not null,
  inputs jsonb not null,
  outputs jsonb not null,
  standard_reference jsonb not null default '{}'::jsonb,
  created_at timestamptz not null default now()
);

alter table public.profiles enable row level security;
alter table public.projects enable row level security;
alter table public.calculations enable row level security;

create policy "profile owner read" on public.profiles
for select using (auth.uid() = id);
create policy "profile owner update" on public.profiles
for update using (auth.uid() = id);

create policy "project owner access" on public.projects
for all using (auth.uid() = owner_id)
with check (auth.uid() = owner_id);

create policy "calculation owner access" on public.calculations
for all using (auth.uid() = owner_id)
with check (auth.uid() = owner_id);

create index if not exists idx_projects_owner on public.projects(owner_id);
create index if not exists idx_calculations_project on public.calculations(project_id);
create index if not exists idx_calculations_owner on public.calculations(owner_id);
