//
//  AvatarView.swift
//  CitasMedicas
//
//  Avatar circular con iniciales y un punto de estado opcional.
//

import SwiftUI

struct AvatarView: View {
    let initials: String
    var size: CGFloat = 48
    var tint: Color = AppColor.primary
    var showOnlineDot: Bool = false
    var imageURL: String? = nil

    var body: some View {
        ZStack(alignment: .bottomTrailing) {
            avatarBody
            if showOnlineDot {
                Circle()
                    .fill(AppColor.mint)
                    .frame(width: size * 0.26, height: size * 0.26)
                    .overlay(Circle().stroke(AppColor.surface, lineWidth: 2))
                    .offset(x: 1, y: 1)
            }
        }
    }

    @ViewBuilder
    private var avatarBody: some View {
        if let imageURL, let url = URL(string: imageURL) {
            AsyncImage(url: url) { phase in
                switch phase {
                case .success(let image):
                    image.resizable().scaledToFill()
                default:
                    initialsCircle
                }
            }
            .frame(width: size, height: size)
            .clipShape(Circle())
        } else {
            initialsCircle
        }
    }

    private var initialsCircle: some View {
        Circle()
            .fill(tint.opacity(0.12))
            .frame(width: size, height: size)
            .overlay(
                Text(initials)
                    .font(.system(size: size * 0.36, weight: .bold, design: .rounded))
                    .foregroundStyle(tint)
            )
    }
}
